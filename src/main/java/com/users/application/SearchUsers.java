package com.users.application;

import com.users.domain.model.PageResult;
import com.users.domain.model.User;
import com.users.domain.model.UserSearch;
import com.users.domain.port.IdentityProviderPort;

public final class SearchUsers {

	private final IdentityProviderPort identityProvider;

	public SearchUsers(IdentityProviderPort identityProvider) {
		this.identityProvider = identityProvider;
	}

	public PageResult<User> handle(UserSearch search) {
		return identityProvider.search(search);
	}
}
