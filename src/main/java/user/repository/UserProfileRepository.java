package user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import user.domain.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {}
