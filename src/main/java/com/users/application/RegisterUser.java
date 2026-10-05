package com.users.application;

import java.time.Clock;

import com.users.domain.event.UserRegistered;
import com.users.domain.model.Email;
import com.users.domain.model.NewUser;
import com.users.domain.model.Password;
import com.users.domain.model.PersonName;
import com.users.domain.model.UserId;
import com.users.domain.model.Username;
import com.users.domain.port.DomainEventPublisher;
import com.users.domain.port.IdentityProviderPort;
import com.users.domain.port.UserProfileRepository;

/** A person signs themselves up. They must verify their email before they can sign in. */
public final class RegisterUser {

	public record Command(String username, String email, String firstName, String lastName, String password) {
	}

	private final IdentityProviderPort identityProvider;
	private final UserCreation creation;
	private final DomainEventPublisher events;
	private final Clock clock;

	public RegisterUser(IdentityProviderPort identityProvider, UserProfileRepository profiles,
			DomainEventPublisher events, Clock clock) {
		this.identityProvider = identityProvider;
		this.creation = new UserCreation(identityProvider, profiles, clock);
		this.events = events;
		this.clock = clock;
	}

	public UserId handle(Command command) {
		var newUser = NewUser.selfRegistered(Username.of(command.username()), Email.of(command.email()),
				PersonName.of(command.firstName(), command.lastName()), Password.of(command.password()));
		UserId id = creation.create(newUser, identityProvider::sendVerificationEmail);
		events.publish(new UserRegistered(id, clock.instant()));
		return id;
	}
}
