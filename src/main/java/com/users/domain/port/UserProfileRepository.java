package com.users.domain.port;

import java.util.Optional;

import com.users.domain.model.UserId;
import com.users.domain.model.UserProfile;

/** Persistence of the extended profile in this service's own database. */
public interface UserProfileRepository {

	Optional<UserProfile> findByUserId(UserId userId);

	UserProfile save(UserProfile profile);

	void deleteByUserId(UserId userId);
}
