package com.users.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import com.users.domain.model.UserId;
import com.users.domain.model.UserProfile;
import com.users.domain.port.UserProfileRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaUserProfileRepository implements UserProfileRepository {

	private final SpringDataUserProfileRepository repository;

	JpaUserProfileRepository(SpringDataUserProfileRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserProfile> findByUserId(UserId userId) {
		return repository.findById(UUID.fromString(userId.value())).map(JpaUserProfileRepository::toDomain);
	}

	@Override
	@Transactional
	public UserProfile save(UserProfile profile) {
		UUID id = UUID.fromString(profile.userId().value());
		UserProfileJpaEntity entity = repository.findById(id)
				.orElseGet(() -> new UserProfileJpaEntity(id, profile.createdAt()));
		entity.apply(profile.phoneNumber(), profile.jobTitle(), profile.department(), profile.locale(),
				profile.timeZone(), profile.bio(), profile.updatedAt());
		return toDomain(repository.saveAndFlush(entity));
	}

	@Override
	@Transactional
	public void deleteByUserId(UserId userId) {
		UUID id = UUID.fromString(userId.value());
		if (repository.existsById(id)) {
			repository.deleteById(id);
		}
	}

	private static UserProfile toDomain(UserProfileJpaEntity entity) {
		return UserProfile.rehydrate(UserId.of(entity.getUserId().toString()), entity.getPhoneNumber(),
				entity.getJobTitle(), entity.getDepartment(), entity.getLocale(), entity.getTimeZone(),
				entity.getBio(), entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
