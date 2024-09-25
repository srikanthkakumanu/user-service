package user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.domain.UserRole;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
}
