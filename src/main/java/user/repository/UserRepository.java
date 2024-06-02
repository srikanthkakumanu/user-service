package user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import user.domain.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query(name="select u from User u where u.email = :email", nativeQuery = false)
    User findByEmail(String email);
}
