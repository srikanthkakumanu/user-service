package com.users.application;

import java.time.Clock;

import com.users.domain.event.UserDeleted;
import com.users.domain.model.Actor;
import com.users.domain.model.UserId;
import com.users.domain.policy.AdministrationPolicy;
import com.users.domain.port.DomainEventPublisher;
import com.users.domain.port.IdentityProviderPort;
import com.users.domain.port.UserProfileRepository;

/** Removes the identity (and with it sessions and credentials) and then the profile. */
public final class DeleteUser {

	private final IdentityProviderPort identityProvider;
	private final UserProfileRepository profiles;
	private final DomainEventPublisher events;
	private final Clock clock;
	private final AdministrationPolicy policy = new AdministrationPolicy();

	public DeleteUser(IdentityProviderPort identityProvider, UserProfileRepository profiles,
			DomainEventPublisher events, Clock clock) {
		this.identityProvider = identityProvider;
		this.profiles = profiles;
		this.events = events;
		this.clock = clock;
	}

	public void handle(Actor actor, UserId id) {
		Users.require(identityProvider, id);
		policy.checkNotSelf(actor, id, "delete");
		policy.checkMayAdminister(actor, identityProvider.isPlatformAdmin(id));
		policy.checkMayRemoveAccess(id, identityProvider.isLastPlatformAdmin(id));
		// Identity first: it is the source of truth. A profile left behind by a failure here is
		// unreachable and harmless; an identity left behind without a profile would still sign in.
		identityProvider.delete(id);
		profiles.deleteByUserId(id);
		events.publish(new UserDeleted(id, clock.instant()));
	}
}
