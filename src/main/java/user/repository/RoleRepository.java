package user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.domain.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByRole(String role);
//    Optional<Role> save (UUID id, String description);
//    Optional<Role> save (UUID id, String role);
//    Optional<Role> save (UUID id, String role, String description);
}
