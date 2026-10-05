package com.users.application;

import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.port.IdentityProviderPort;

public final class GetUser {

	private final IdentityProviderPort identityProvider;

	public GetUser(IdentityProviderPort identityProvider) {
		this.identityProvider = identityProvider;
	}

	public User handle(UserId id) {
		return Users.require(identityProvider, id);
	}
}
