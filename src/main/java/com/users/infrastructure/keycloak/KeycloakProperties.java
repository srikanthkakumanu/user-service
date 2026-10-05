package com.users.infrastructure.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Connection to the identity provider's admin API.
 *
 * @param serverUrl base URL the service uses to reach Keycloak
 * @param realm the platform realm
 * @param clientId this service's confidential client
 * @param clientSecret its secret, supplied by Vault
 * @param platformAdminRole realm role that marks a platform administrator
 */
@ConfigurationProperties("platform.keycloak")
public record KeycloakProperties(String serverUrl, String realm, @DefaultValue("user-service") String clientId,
		String clientSecret, @DefaultValue("PLATFORM_ADMIN") String platformAdminRole) {
}
