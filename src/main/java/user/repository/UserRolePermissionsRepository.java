package user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.domain.RolePermissions;

import java.util.UUID;

public interface UserRolePermissionsRepository extends JpaRepository<RolePermissions, UUID> {
}
