package com.users.application;

import java.time.Clock;

import com.users.domain.event.UserUnlocked;
import com.users.domain.model.Actor;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.policy.AdministrationPolicy;
import com.users.domain.port.DomainEventPublisher;
import com.users.domain.port.IdentityProviderPort;

/** Lifts an administrative lock and clears failed-login lockout. */
public final class UnlockUser {

	private final IdentityProviderPort identityProvider;
	private final DomainEventPublisher events;
	private final Clock clock;
	private final AdministrationPolicy policy = new AdministrationPolicy();

	public UnlockUser(IdentityProviderPort identityProvider, DomainEventPublisher events, Clock clock) {
		this.identityProvider = identityProvider;
		this.events = events;
		this.clock = clock;
	}

	public User handle(Actor actor, UserId id) {
		User user = Users.require(identityProvider, id);
		policy.checkMayAdminister(actor, identityProvider.isPlatformAdmin(id));
		user.unlock();
		identityProvider.save(user);
		events.publish(new UserUnlocked(id, clock.instant()));
		return user;
	}
}
