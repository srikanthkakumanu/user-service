package com.users.application;

import java.time.Clock;

import com.users.domain.event.UserPasswordReset;
import com.users.domain.model.Actor;
import com.users.domain.model.Password;
import com.users.domain.model.UserId;
import com.users.domain.policy.AdministrationPolicy;
import com.users.domain.port.DomainEventPublisher;
import com.users.domain.port.IdentityProviderPort;

/**
 * An administrator sets a user's password. A temporary password must be changed at next sign-in.
 * Existing sessions end, because whoever knew the old password may still hold them.
 */
public final class ResetPassword {

	private final IdentityProviderPort identityProvider;
	private final DomainEventPublisher events;
	private final Clock clock;
	private final AdministrationPolicy policy = new AdministrationPolicy();

	public ResetPassword(IdentityProviderPort identityProvider, DomainEventPublisher events, Clock clock) {
		this.identityProvider = identityProvider;
		this.events = events;
		this.clock = clock;
	}

	public void handle(Actor actor, UserId id, String password, boolean temporary) {
		Users.require(identityProvider, id);
		policy.checkMayAdminister(actor, identityProvider.isPlatformAdmin(id));
		identityProvider.setPassword(id, Password.of(password), temporary);
		identityProvider.signOutEverywhere(id);
		events.publish(new UserPasswordReset(id, clock.instant()));
	}
}
