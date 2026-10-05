package com.users.application;

import java.time.Clock;
import java.util.function.Consumer;

import com.users.domain.model.NewUser;
import com.users.domain.model.UserId;
import com.users.domain.model.UserProfile;
import com.users.domain.port.IdentityProviderPort;
import com.users.domain.port.UserProfileRepository;

/**
 * Creating a user spans two systems: the identity in the identity provider and the profile in
 * this service's database. There is no shared transaction, so this is a process with
 * compensation: if anything fails after the identity exists, the identity is deleted again.
 * See docs/adr/0006-user-creation-consistency.md.
 */
final class UserCreation {

	private final IdentityProviderPort identityProvider;
	private final UserProfileRepository profiles;
	private final Clock clock;

	UserCreation(IdentityProviderPort identityProvider, UserProfileRepository profiles, Clock clock) {
		this.identityProvider = identityProvider;
		this.profiles = profiles;
		this.clock = clock;
	}

	/** @param afterCreated further steps that belong to the same all-or-nothing creation */
	UserId create(NewUser newUser, Consumer<UserId> afterCreated) {
		UserId id = identityProvider.create(newUser);
		try {
			profiles.save(UserProfile.empty(id, clock.instant()));
			afterCreated.accept(id);
			return id;
		}
		catch (RuntimeException failure) {
			compensate(id, failure);
			throw failure;
		}
	}

	private void compensate(UserId id, RuntimeException failure) {
		try {
			profiles.deleteByUserId(id);
		}
		catch (RuntimeException ex) {
			failure.addSuppressed(ex);
		}
		try {
			identityProvider.delete(id);
		}
		catch (RuntimeException ex) {
			failure.addSuppressed(ex);
		}
	}
}
