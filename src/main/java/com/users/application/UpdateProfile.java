package com.users.application;

import java.time.Clock;

import com.users.domain.event.UserProfileUpdated;
import com.users.domain.model.PersonName;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.model.UserProfile;
import com.users.domain.port.DomainEventPublisher;
import com.users.domain.port.IdentityProviderPort;
import com.users.domain.port.UserProfileRepository;

/** Changes a user's name and extended profile; used by the user themselves and by administrators. */
public final class UpdateProfile {

	public record Command(UserId id, String firstName, String lastName, String phoneNumber, String jobTitle,
			String department, String locale, String timeZone, String bio) {
	}

	private final IdentityProviderPort identityProvider;
	private final UserProfileRepository profiles;
	private final DomainEventPublisher events;
	private final Clock clock;

	public UpdateProfile(IdentityProviderPort identityProvider, UserProfileRepository profiles,
			DomainEventPublisher events, Clock clock) {
		this.identityProvider = identityProvider;
		this.profiles = profiles;
		this.events = events;
		this.clock = clock;
	}

	public GetProfile.Result handle(Command command) {
		User user = Users.require(identityProvider, command.id());
		var now = clock.instant();
		UserProfile profile = profiles.findByUserId(user.id()).orElseGet(() -> UserProfile.empty(user.id(), now));
		// Validate everything before writing to either system.
		var name = PersonName.of(command.firstName(), command.lastName());
		profile.update(command.phoneNumber(), command.jobTitle(), command.department(), command.locale(),
				command.timeZone(), command.bio(), now);
		if (!name.equals(user.name())) {
			user.rename(name);
			identityProvider.save(user);
		}
		UserProfile saved = profiles.save(profile);
		events.publish(new UserProfileUpdated(user.id(), now));
		return new GetProfile.Result(user, saved);
	}
}
