package com.users.application;

import java.time.Clock;

import com.users.domain.event.UserUpdated;
import com.users.domain.model.Actor;
import com.users.domain.model.Email;
import com.users.domain.model.PersonName;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.policy.AdministrationPolicy;
import com.users.domain.port.DomainEventPublisher;
import com.users.domain.port.IdentityProviderPort;

/** An administrator changes a user's email or name. */
public final class UpdateUser {

	public record Command(UserId id, String email, String firstName, String lastName) {
	}

	private final IdentityProviderPort identityProvider;
	private final DomainEventPublisher events;
	private final Clock clock;
	private final AdministrationPolicy policy = new AdministrationPolicy();

	public UpdateUser(IdentityProviderPort identityProvider, DomainEventPublisher events, Clock clock) {
		this.identityProvider = identityProvider;
		this.events = events;
		this.clock = clock;
	}

	public User handle(Actor actor, Command command) {
		User user = Users.require(identityProvider, command.id());
		// A new email address could be used to take the account over through a password reset.
		policy.checkMayAdminister(actor, identityProvider.isPlatformAdmin(user.id()));
		user.changeEmail(Email.of(command.email()));
		user.rename(PersonName.of(command.firstName(), command.lastName()));
		identityProvider.save(user);
		events.publish(new UserUpdated(user.id(), clock.instant()));
		return user;
	}
}
