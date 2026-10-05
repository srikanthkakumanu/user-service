package com.users.application;

import java.util.List;

import com.users.domain.model.Credential;
import com.users.domain.model.UserId;
import com.users.domain.port.IdentityProviderPort;

public final class ListCredentials {

	private final IdentityProviderPort identityProvider;

	public ListCredentials(IdentityProviderPort identityProvider) {
		this.identityProvider = identityProvider;
	}

	public List<Credential> handle(UserId id) {
		Users.require(identityProvider, id);
		return identityProvider.listCredentials(id);
	}
}
