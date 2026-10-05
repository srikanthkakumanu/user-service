package com.users.application;

import java.time.Clock;

import com.users.domain.event.UserDisabled;
import com.users.domain.model.Actor;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.policy.AdministrationPolicy;
import com.users.domain.port.DomainEventPublisher;
import com.users.domain.port.IdentityProviderPort;

/** Switches an account off and ends its sessions, so its refresh tokens stop working at once. */
public final class DisableUser {

	private final IdentityProviderPort identityProvider;
	private final DomainEventPublisher events;
	private final Clock clock;
	private final AdministrationPolicy policy = new AdministrationPolicy();

	public DisableUser(IdentityProviderPort identityProvider, DomainEventPublisher events, Clock clock) {
		this.identityProvider = identityProvider;
		this.events = events;
		this.clock = clock;
	}

	public User handle(Actor actor, UserId id) {
		User user = Users.require(identityProvider, id);
		policy.checkMayAdminister(actor, identityProvider.isPlatformAdmin(id));
		policy.checkNotSelf(actor, id, "disable");
		policy.checkMayRemoveAccess(id, identityProvider.isLastPlatformAdmin(id));
		user.disable();
		identityProvider.save(user);
		identityProvider.signOutEverywhere(id);
		events.publish(new UserDisabled(id, clock.instant()));
		return user;
	}
}
