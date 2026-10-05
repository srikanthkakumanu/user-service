package com.users;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.users.infrastructure.keycloak.KeycloakTestEnvironment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * The whole service over HTTP with real Postgres, a real Keycloak running the platform realm and
 * tokens issued by that Keycloak.
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserServiceIT {

	private static final String PASSWORD = "S3cret!Passw0rd";
	private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {
	};

	@Container
	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

	@Value("${local.server.port}")
	private int port;

	@org.springframework.beans.factory.annotation.Autowired
	private JdbcTemplate jdbc;

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("platform.keycloak.server-url", KeycloakTestEnvironment::serverUrl);
		registry.add("platform.keycloak.realm", () -> KeycloakTestEnvironment.REALM);
		registry.add("platform.keycloak.client-secret", () -> KeycloakTestEnvironment.USER_SERVICE_SECRET);
		registry.add("platform.security.jwt.issuer-uri", KeycloakTestEnvironment::issuerUri);
		registry.add("platform.security.jwt.jwk-set-uri",
				() -> KeycloakTestEnvironment.issuerUri() + "/protocol/openid-connect/certs");
	}

	@Test
	void selfServiceRegisterVerifySignInAndManageOwnProfile() {
		String name = unique();

		var registered = call(HttpMethod.POST, "/api/v1/users/register", null, registration(name));
		String id = (String) registered.getBody().get("id");
		assertThat(registered.getStatusCode().value()).isEqualTo(201);
		assertThat(registered.getHeaders().getLocation()).hasToString("/api/v1/users/" + id);
		assertThat(KeycloakTestEnvironment.emailSubjectsFor(name + "@example.com")).hasSize(1);
		assertThat(profileRows(id)).isEqualTo(1);

		// Until the email address is verified the account cannot sign in.
		assertThatExceptionOfType(HttpClientErrorException.class)
				.isThrownBy(() -> KeycloakTestEnvironment.accessToken(name, PASSWORD));
		markEmailVerified(id);
		String token = KeycloakTestEnvironment.accessToken(name, PASSWORD);

		var me = call(HttpMethod.GET, "/api/v1/users/me", token, null);
		assertThat(me.getStatusCode().value()).isEqualTo(200);
		assertThat(me.getBody()).containsEntry("id", id).containsEntry("username", name)
				.containsEntry("emailVerified", true).containsEntry("firstName", "Test");

		var updated = call(HttpMethod.PUT, "/api/v1/users/me", token, Map.of("firstName", "Alicia", "lastName", "Roe",
				"jobTitle", "Engineer", "timeZone", "Europe/London", "phoneNumber", "+44 20 7946 0958"));
		assertThat(updated.getStatusCode().value()).isEqualTo(200);
		assertThat(call(HttpMethod.GET, "/api/v1/users/me", token, null).getBody())
				.containsEntry("firstName", "Alicia").containsEntry("jobTitle", "Engineer")
				.containsEntry("timeZone", "Europe/London");
		assertThat(call(HttpMethod.GET, "/api/v1/users/" + id, token, null).getBody()).containsEntry("lastName", "Roe");

		// A self-service user holds no user-administration permission.
		assertProblem(call(HttpMethod.GET, "/api/v1/users", token, null), 403, "forbidden");
		assertProblem(call(HttpMethod.GET, "/api/v1/users/" + platformAdminId(), token, null), 403, "forbidden");
		assertProblem(call(HttpMethod.PUT, "/api/v1/users/me", token, Map.of("firstName", "A", "lastName", "B",
				"timeZone", "Mars/Olympus")), 400, "invalid-value");
	}

	@Test
	void registeringTheSameUsernameOrEmailTwiceIsAConflictAndCreatesNothing() {
		String name = unique();
		assertThat(call(HttpMethod.POST, "/api/v1/users/register", null, registration(name)).getStatusCode().value())
				.isEqualTo(201);

		assertProblem(call(HttpMethod.POST, "/api/v1/users/register", null, registration(name)), 409, "duplicate-user");
		assertProblem(call(HttpMethod.POST, "/api/v1/users/register", null, Map.of("username", unique(), "email",
				name + "@example.com", "firstName", "Test", "lastName", "User", "password", PASSWORD)), 409,
				"duplicate-user");
		assertProblem(call(HttpMethod.POST, "/api/v1/users/register", null, Map.of("username", unique(), "email",
				unique() + "@example.com", "firstName", "Test", "lastName", "User", "password", "weakpassword")), 422,
				"password-policy");
	}

	@Test
	void anAdministratorManagesAUsersWholeLifecycle() {
		String admin = KeycloakTestEnvironment.platformAdminToken();
		String name = unique();

		var created = call(HttpMethod.POST, "/api/v1/users", admin, Map.of("username", name, "email",
				name + "@example.com", "firstName", "Bob", "lastName", "Roe", "emailVerified", true));
		String id = (String) created.getBody().get("id");
		assertThat(created.getStatusCode().value()).isEqualTo(201);

		var found = call(HttpMethod.GET, "/api/v1/users?q=" + name + "&size=5", admin, null);
		assertThat(found.getBody()).containsEntry("total", 1);
		assertThat(items(found)).extracting(item -> item.get("id")).containsExactly(id);

		// A permanent password lets the user sign in.
		assertThat(call(HttpMethod.PUT, "/api/v1/users/" + id + "/credentials/password", admin,
				Map.of("password", PASSWORD, "temporary", false)).getStatusCode().value()).isEqualTo(204);
		assertThat(KeycloakTestEnvironment.accessToken(name, PASSWORD)).isNotBlank();

		assertThat(call(HttpMethod.POST, "/api/v1/users/" + id + "/disable", admin, null).getBody())
				.containsEntry("status", "DISABLED");
		assertSignInRejected(name);
		assertThat(call(HttpMethod.POST, "/api/v1/users/" + id + "/enable", admin, null).getBody())
				.containsEntry("status", "ACTIVE");
		assertThat(KeycloakTestEnvironment.accessToken(name, PASSWORD)).isNotBlank();

		assertThat(call(HttpMethod.POST, "/api/v1/users/" + id + "/lock", admin, null).getBody())
				.containsEntry("status", "LOCKED");
		assertSignInRejected(name);
		assertProblem(call(HttpMethod.POST, "/api/v1/users/" + id + "/enable", admin, null), 409, "invalid-state");
		assertThat(call(HttpMethod.POST, "/api/v1/users/" + id + "/unlock", admin, null).getBody())
				.containsEntry("status", "ACTIVE");

		// A temporary password forces a change, so a plain sign-in is refused.
		call(HttpMethod.PUT, "/api/v1/users/" + id + "/credentials/password", admin,
				Map.of("password", "Temp0rary!Passw0rd", "temporary", true));
		assertThat(call(HttpMethod.GET, "/api/v1/users/" + id, admin, null).getBody().get("requiredActions"))
				.asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.LIST).contains("UPDATE_PASSWORD");
		assertThatExceptionOfType(HttpClientErrorException.class)
				.isThrownBy(() -> KeycloakTestEnvironment.accessToken(name, "Temp0rary!Passw0rd"));

		List<Map<String, Object>> credentials = RestClient.create(baseUrl()).get()
				.uri("/api/v1/users/" + id + "/credentials").headers(headers -> headers.setBearerAuth(admin)).retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
		assertThat(credentials).hasSize(1);
		assertThat(call(HttpMethod.DELETE, "/api/v1/users/" + id + "/credentials/" + credentials.getFirst().get("id"),
				admin, null).getStatusCode().value()).isEqualTo(204);

		assertThat(call(HttpMethod.PUT, "/api/v1/users/" + id, admin, Map.of("email", "new-" + name + "@example.com",
				"firstName", "Robert", "lastName", "Roe")).getBody())
				.containsEntry("email", "new-" + name + "@example.com").containsEntry("emailVerified", false);
		assertThat(call(HttpMethod.POST, "/api/v1/users/" + id + "/actions/send-verify-email", admin, null)
				.getStatusCode().value()).isEqualTo(202);
		assertThat(KeycloakTestEnvironment.emailSubjectsFor("new-" + name + "@example.com")).hasSize(1);
		assertThat(call(HttpMethod.PUT, "/api/v1/users/" + id + "/actions/required", admin,
				Map.of("actions", List.of("UPDATE_PROFILE"))).getBody().get("requiredActions"))
				.asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.LIST).containsExactly("UPDATE_PROFILE");

		assertThat(call(HttpMethod.DELETE, "/api/v1/users/" + id, admin, null).getStatusCode().value()).isEqualTo(204);
		assertProblem(call(HttpMethod.GET, "/api/v1/users/" + id, admin, null), 404, "user-not-found");
		assertThat(profileRows(id)).isZero();
	}

	@Test
	void guardsProtectThePlatformAdministrator() {
		String admin = KeycloakTestEnvironment.platformAdminToken();
		String adminId = platformAdminId();

		assertProblem(call(HttpMethod.DELETE, "/api/v1/users/" + adminId, admin, null), 403, "operation-not-permitted");
		assertProblem(call(HttpMethod.POST, "/api/v1/users/" + adminId + "/disable", admin, null), 403,
				"operation-not-permitted");
		assertProblem(call(HttpMethod.POST, "/api/v1/users/" + adminId + "/lock", admin, null), 403,
				"operation-not-permitted");
		assertThat(call(HttpMethod.GET, "/api/v1/users/" + adminId, admin, null).getBody())
				.containsEntry("status", "ACTIVE");
	}

	@Test
	void rejectsMissingAndTamperedTokensAndServesPublicEndpoints() {
		String token = KeycloakTestEnvironment.platformAdminToken();
		String tampered = token.substring(0, token.length() - 6) + (token.endsWith("AAAAAA") ? "BBBBBB" : "AAAAAA");

		assertProblem(call(HttpMethod.GET, "/api/v1/users", null, null), 401, "unauthorized");
		assertProblem(call(HttpMethod.GET, "/api/v1/users", tampered, null), 401, "unauthorized");
		assertThat(call(HttpMethod.GET, "/actuator/health", null, null).getBody()).containsEntry("status", "UP");
		assertThat(call(HttpMethod.GET, "/v3/api-docs", null, null).getBody()).containsKey("paths");
		assertThat(call(HttpMethod.POST, "/api/v1/users/password-reset-requests", null,
				Map.of("email", "nobody-" + unique() + "@example.com")).getStatusCode().value()).isEqualTo(202);
	}

	// --- helpers

	private ResponseEntity<Map<String, Object>> call(HttpMethod method, String path, String token, Object body) {
		var request = RestClient.builder().baseUrl(baseUrl()).defaultStatusHandler(status -> true, (req, res) -> {
		}).build().method(method).uri(path);
		if (token != null) {
			request.headers(headers -> headers.setBearerAuth(token));
		}
		if (body != null) {
			request.contentType(MediaType.APPLICATION_JSON).body(body);
		}
		return request.retrieve().toEntity(JSON);
	}

	private static void assertProblem(ResponseEntity<Map<String, Object>> response, int status, String code) {
		assertThat(response.getStatusCode().value()).isEqualTo(status);
		assertThat(response.getHeaders().getContentType()).isNotNull()
				.matches(type -> type.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
		assertThat(response.getBody()).containsEntry("code", code)
				.containsEntry("type", "https://platform.local/problems/" + code).containsEntry("status", status);
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> items(ResponseEntity<Map<String, Object>> response) {
		return (List<Map<String, Object>>) response.getBody().get("items");
	}

	private static void assertSignInRejected(String username) {
		assertThatExceptionOfType(HttpClientErrorException.class)
				.isThrownBy(() -> KeycloakTestEnvironment.accessToken(username, PASSWORD));
	}

	private static void markEmailVerified(String id) {
		var resource = KeycloakTestEnvironment.masterAdmin().realm(KeycloakTestEnvironment.REALM).users().get(id);
		var representation = resource.toRepresentation();
		representation.setEmailVerified(true);
		representation.setRequiredActions(List.of());
		resource.update(representation);
	}

	private static String platformAdminId() {
		return KeycloakTestEnvironment.masterAdmin().realm(KeycloakTestEnvironment.REALM).users()
				.searchByUsername("platform-admin", true).getFirst().getId();
	}

	private int profileRows(String id) {
		return jdbc.queryForObject("select count(*) from user_profile where user_id = ?::uuid", Integer.class, id);
	}

	private static Map<String, Object> registration(String name) {
		return Map.of("username", name, "email", name + "@example.com", "firstName", "Test", "lastName", "User",
				"password", PASSWORD);
	}

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	private static String unique() {
		return "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}
