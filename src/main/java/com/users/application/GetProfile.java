package com.users.application;

import java.time.Clock;

import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.model.UserProfile;
import com.users.domain.port.IdentityProviderPort;
import com.users.domain.port.UserProfileRepository;

/** A user's identity together with their extended profile. */
public final class GetProfile {

	public record Result(User user, UserProfile profile) {
	}

	private final IdentityProviderPort identityProvider;
	private final UserProfileRepository profiles;
	private final Clock clock;

	public GetProfile(IdentityProviderPort identityProvider, UserProfileRepository profiles, Clock clock) {
		this.identityProvider = identityProvider;
		this.profiles = profiles;
		this.clock = clock;
	}

	public Result handle(UserId id) {
		User user = Users.require(identityProvider, id);
		// Users created outside this service (the bootstrap administrator) have no profile row yet.
		UserProfile profile = profiles.findByUserId(id).orElseGet(() -> UserProfile.empty(id, clock.instant()));
		return new Result(user, profile);
	}
}
