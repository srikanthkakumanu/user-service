package user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import user.domain.UserDomain;

public interface UserRepository extends JpaRepository<UserDomain, UUID> {

    @Query(name="select u from User u where u.loginId = :loginId", nativeQuery = false)
    Optional<UserDomain> findByLoginId(String loginId);
}
