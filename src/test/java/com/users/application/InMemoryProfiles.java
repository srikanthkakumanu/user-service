package com.users.application;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.users.domain.model.UserId;
import com.users.domain.model.UserProfile;
import com.users.domain.port.UserProfileRepository;

class InMemoryProfiles implements UserProfileRepository {

	final Map<UserId, UserProfile> profiles = new LinkedHashMap<>();
	RuntimeException failOnSave;

	@Override
	public Optional<UserProfile> findByUserId(UserId userId) {
		return Optional.ofNullable(profiles.get(userId));
	}

	@Override
	public UserProfile save(UserProfile profile) {
		if (failOnSave != null) {
			throw failOnSave;
		}
		profiles.put(profile.userId(), profile);
		return profile;
	}

	@Override
	public void deleteByUserId(UserId userId) {
		profiles.remove(userId);
	}
}
