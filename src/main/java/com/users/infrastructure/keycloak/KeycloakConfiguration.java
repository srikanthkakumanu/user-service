package com.users.infrastructure.keycloak;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KeycloakProperties.class)
class KeycloakConfiguration {

	/** Authenticates as this service's own service account; the client renews its token itself. */
	@Bean(destroyMethod = "close")
	Keycloak keycloakAdminClient(KeycloakProperties properties) {
		Keycloak keycloak = KeycloakBuilder.builder()
				.serverUrl(properties.serverUrl())
				.realm(properties.realm())
				.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
				.clientId(properties.clientId())
				.clientSecret(properties.clientSecret())
				.build();
		KeycloakIdentityProviderAdapter.onUnauthorized(() -> keycloak.tokenManager().grantToken());
		return keycloak;
	}

	@Bean
	UserQueryResource userQueryResource(Keycloak keycloak, KeycloakProperties properties) {
		return keycloak.proxy(UserQueryResource.class, java.net.URI.create(properties.serverUrl()));
	}

	@Bean
	RealmResource platformRealm(Keycloak keycloak, KeycloakProperties properties) {
		return keycloak.realm(properties.realm());
	}
}
