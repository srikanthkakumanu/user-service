package com.users.application;

import com.users.domain.model.Actor;
import com.users.domain.model.UserId;
import com.users.domain.policy.AdministrationPolicy;
import com.users.domain.port.IdentityProviderPort;

/** Removes one of a user's credentials, for example a password that must no longer be usable. */
public final class RemoveCredential {

	private final IdentityProviderPort identityProvider;
	private final AdministrationPolicy policy = new AdministrationPolicy();

	public RemoveCredential(IdentityProviderPort identityProvider) {
		this.identityProvider = identityProvider;
	}

	public void handle(Actor actor, UserId id, String credentialId) {
		Users.require(identityProvider, id);
		policy.checkMayAdminister(actor, identityProvider.isPlatformAdmin(id));
		identityProvider.removeCredential(id, credentialId);
		identityProvider.signOutEverywhere(id);
	}
}
