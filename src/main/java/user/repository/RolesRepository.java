package user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.domain.Roles;
import java.util.UUID;

public interface RolesRepository extends JpaRepository<Roles, UUID> {
}
