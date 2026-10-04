package user.integration.keycloak;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import user.dto.NewUserDTO;
import user.dto.UserProfileDTO;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class KeycloakAdminClient implements KeycloakUserProvisioningAdapter {

    private final RestClient restClient;
    private final KeycloakAdminProperties properties;

    public KeycloakAdminClient(RestClient.Builder builder, KeycloakAdminProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
    }

    @Override
    public String createUser(NewUserDTO request) {
        var profile = request.getProfile();
        var response = restClient.post()
                .uri("/admin/realms/{realm}/users", properties.realm())
                .headers(headers -> headers.setBearerAuth(adminToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "username", request.getLoginId(),
                        "email", request.getLoginId(),
                        "firstName", valueOrEmpty(profile, UserProfileDTO::getFirstName),
                        "lastName", valueOrEmpty(profile, UserProfileDTO::getLastName),
                        "enabled", true,
                        "emailVerified", false,
                        "credentials", credentials(request.getTemporaryPassword())
                ))
                .retrieve()
                .toBodilessEntity();

        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new IllegalStateException("Keycloak did not return a user location");
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    @Override
    public void updateUser(String keycloakUserId, NewUserDTO request) {
        var profile = request.getProfile();
        restClient.put()
                .uri("/admin/realms/{realm}/users/{id}", properties.realm(), keycloakUserId)
                .headers(headers -> headers.setBearerAuth(adminToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "email", request.getLoginId(),
                        "firstName", valueOrEmpty(profile, UserProfileDTO::getFirstName),
                        "lastName", valueOrEmpty(profile, UserProfileDTO::getLastName)
                ))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void setAccountEnabled(String keycloakUserId, boolean enabled) {
        restClient.put()
                .uri("/admin/realms/{realm}/users/{id}", properties.realm(), keycloakUserId)
                .headers(headers -> headers.setBearerAuth(adminToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("enabled", enabled))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void resetPassword(String keycloakUserId, String temporaryPassword, boolean temporary) {
        restClient.put()
                .uri("/admin/realms/{realm}/users/{id}/reset-password", properties.realm(), keycloakUserId)
                .headers(headers -> headers.setBearerAuth(adminToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "type", "password",
                        "value", temporaryPassword,
                        "temporary", temporary
                ))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void assignRealmRole(String keycloakUserId, String role) {
        updateRealmRole(keycloakUserId, role, true);
    }

    @Override
    public void removeRealmRole(String keycloakUserId, String role) {
        updateRealmRole(keycloakUserId, role, false);
    }

    private void updateRealmRole(String keycloakUserId, String role, boolean assign) {
        var roleRepresentation = restClient.get()
                .uri("/admin/realms/{realm}/roles/{role}", properties.realm(), role)
                .headers(headers -> headers.setBearerAuth(adminToken()))
                .retrieve()
                .body(Map.class);

        restClient.method(assign ? HttpMethod.POST : HttpMethod.DELETE)
                .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", properties.realm(), keycloakUserId)
                .headers(headers -> headers.setBearerAuth(adminToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(roleRepresentation))
                .retrieve()
                .toBodilessEntity();
    }

    private String adminToken() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());

        Map<?, ?> response = restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", properties.realm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        return Objects.requireNonNull(response).get("access_token").toString();
    }

    private List<Map<String, Object>> credentials(String temporaryPassword) {
        if (temporaryPassword == null || temporaryPassword.isBlank()) {
            return List.of();
        }
        return List.of(Map.of(
                "type", "password",
                "value", temporaryPassword,
                "temporary", true
        ));
    }

    private String valueOrEmpty(UserProfileDTO profile, java.util.function.Function<UserProfileDTO, String> accessor) {
        if (profile == null || accessor.apply(profile) == null) {
            return "";
        }
        return accessor.apply(profile);
    }
}
