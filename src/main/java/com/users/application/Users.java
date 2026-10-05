package com.users.application;

import com.users.domain.exception.UserNotFoundException;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.port.IdentityProviderPort;

/** Lookup shared by the use cases. */
final class Users {

	private Users() {
	}

	static User require(IdentityProviderPort identityProvider, UserId id) {
		return identityProvider.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}
}
