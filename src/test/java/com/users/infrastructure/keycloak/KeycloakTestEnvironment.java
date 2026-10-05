package com.users.infrastructure.keycloak;

import java.util.List;
import java.util.Map;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * A real Keycloak loaded with the platform realm file, plus Mailpit under the host name the realm
 * expects. Started once per test run. Mirrors what the bootstrap job does for secrets.
 */
public final class KeycloakTestEnvironment {

	public static final String REALM = "platform";
	public static final String USER_SERVICE_SECRET = "test-user-service-secret";
	public static final String AUTH_SERVICE_SECRET = "test-auth-service-secret";
	public static final String PLATFORM_ADMIN_PASSWORD = "Admin!Passw0rd-1";

	private static final Network NETWORK = Network.newNetwork();

	@SuppressWarnings("resource")
	private static final GenericContainer<?> MAILPIT = new GenericContainer<>("axllent/mailpit:v1.31")
			.withNetwork(NETWORK).withNetworkAliases("mailpit").withExposedPorts(8025)
			.waitingFor(Wait.forHttp("/readyz").forPort(8025));

	@SuppressWarnings("resource")
	private static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:26.8.0")
			.withNetwork(NETWORK).withRealmImportFile("/platform-realm.json");

	static {
		MAILPIT.start();
		KEYCLOAK.start();
		applySecrets();
	}

	private KeycloakTestEnvironment() {
	}

	public static String serverUrl() {
		return KEYCLOAK.getAuthServerUrl();
	}

	public static String issuerUri() {
		return serverUrl() + "/realms/" + REALM;
	}

	public static KeycloakProperties userServiceProperties() {
		return new KeycloakProperties(serverUrl(), REALM, "user-service", USER_SERVICE_SECRET, "PLATFORM_ADMIN");
	}

	/** The master-realm administrator, for arranging state a service account may not touch. */
	public static Keycloak masterAdmin() {
		return KEYCLOAK.getKeycloakAdminClient();
	}

	/** Access token of a user, obtained the way auth-service will: a direct grant on its client. */
	public static String accessToken(String username, String password) {
		var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
		form.add("grant_type", "password");
		form.add("client_id", "auth-service");
		form.add("client_secret", AUTH_SERVICE_SECRET);
		form.add("username", username);
		form.add("password", password);
		Map<String, Object> response = RestClient.create(issuerUri()).post().uri("/protocol/openid-connect/token")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
		return (String) response.get("access_token");
	}

	public static String platformAdminToken() {
		return accessToken("platform-admin", PLATFORM_ADMIN_PASSWORD);
	}

	/** Subjects of the emails Mailpit has received for the given recipient. */
	public static List<String> emailSubjectsFor(String recipient) {
		Map<String, Object> response = RestClient.create(mailpitUrl()).get()
				.uri(uri -> uri.path("/api/v1/search").queryParam("query", "to:" + recipient).build()).retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> messages = (List<Map<String, Object>>) response.get("messages");
		return messages.stream().map(message -> (String) message.get("Subject")).toList();
	}

	public static String mailpitUrl() {
		return "http://" + MAILPIT.getHost() + ":" + MAILPIT.getMappedPort(8025);
	}

	private static void applySecrets() {
		var realm = masterAdmin().realm(REALM);
		setSecret(realm, "user-service", USER_SERVICE_SECRET);
		setSecret(realm, "auth-service", AUTH_SERVICE_SECRET);
		var admin = realm.users().searchByUsername("platform-admin", true).getFirst();
		var password = new CredentialRepresentation();
		password.setType(CredentialRepresentation.PASSWORD);
		password.setValue(PLATFORM_ADMIN_PASSWORD);
		password.setTemporary(false);
		realm.users().get(admin.getId()).resetPassword(password);
	}

	private static void setSecret(org.keycloak.admin.client.resource.RealmResource realm, String clientId, String secret) {
		ClientRepresentation client = realm.clients().findByClientId(clientId).getFirst();
		client.setSecret(secret);
		realm.clients().get(client.getId()).update(client);
	}
}
