package user.integration.keycloak;

import user.dto.NewUserDTO;

public interface KeycloakUserProvisioningAdapter {

    String createUser(NewUserDTO request);

    void updateUser(String keycloakUserId, NewUserDTO request);

    void setAccountEnabled(String keycloakUserId, boolean enabled);

    void resetPassword(String keycloakUserId, String temporaryPassword, boolean temporary);

    void assignRealmRole(String keycloakUserId, String role);

    void removeRealmRole(String keycloakUserId, String role);
}
