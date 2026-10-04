package user.integration.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iam.keycloak.admin")
public record KeycloakAdminProperties(
        String baseUrl,
        String realm,
        String clientId,
        String clientSecret) {

    public String issuerUri() {
        return "%s/realms/%s".formatted(baseUrl, realm);
    }
}
