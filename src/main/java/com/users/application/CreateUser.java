package com.users.application;

import java.time.Clock;

import com.users.domain.event.UserCreated;
import com.users.domain.model.Email;
import com.users.domain.model.NewUser;
import com.users.domain.model.Password;
import com.users.domain.model.PersonName;
import com.users.domain.model.UserId;
import com.users.domain.model.Username;
import com.users.domain.port.DomainEventPublisher;
import com.users.domain.port.IdentityProviderPort;
import com.users.domain.port.UserProfileRepository;

/** An administrator creates an account. A supplied password is temporary. */
public final class CreateUser {

	/** @param temporaryPassword optional; when absent the user gets a password by email or reset */
	public record Command(String username, String email, String firstName, String lastName, String temporaryPassword,
			boolean emailVerified) {
	}

	private final UserCreation creation;
	private final DomainEventPublisher events;
	private final Clock clock;

	public CreateUser(IdentityProviderPort identityProvider, UserProfileRepository profiles,
			DomainEventPublisher events, Clock clock) {
		this.creation = new UserCreation(identityProvider, profiles, clock);
		this.events = events;
		this.clock = clock;
	}

	public UserId handle(Command command) {
		Password password = command.temporaryPassword() == null ? null : Password.of(command.temporaryPassword());
		var newUser = NewUser.createdByAdmin(Username.of(command.username()), Email.of(command.email()),
				PersonName.of(command.firstName(), command.lastName()), password, command.emailVerified());
		UserId id = creation.create(newUser, created -> {
		});
		events.publish(new UserCreated(id, clock.instant()));
		return id;
	}
}
