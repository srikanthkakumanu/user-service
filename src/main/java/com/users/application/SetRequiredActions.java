package com.users.application;

import java.util.Set;

import com.users.domain.model.Actor;
import com.users.domain.model.RequiredAction;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.policy.AdministrationPolicy;
import com.users.domain.port.IdentityProviderPort;

/** Replaces what a user must do at next sign-in. */
public final class SetRequiredActions {

	private final IdentityProviderPort identityProvider;
	private final AdministrationPolicy policy = new AdministrationPolicy();

	public SetRequiredActions(IdentityProviderPort identityProvider) {
		this.identityProvider = identityProvider;
	}

	public User handle(Actor actor, UserId id, Set<RequiredAction> actions) {
		User user = Users.require(identityProvider, id);
		policy.checkMayAdminister(actor, identityProvider.isPlatformAdmin(id));
		user.require(actions);
		identityProvider.setRequiredActions(id, user.requiredActions());
		return user;
	}
}
