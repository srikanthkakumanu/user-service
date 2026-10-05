package com.users.application;

import com.users.domain.exception.InvalidStateException;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.port.IdentityProviderPort;

/** An administrator sends a user a link to choose a new password. */
public final class SendPasswordResetEmail {

	private final IdentityProviderPort identityProvider;

	public SendPasswordResetEmail(IdentityProviderPort identityProvider) {
		this.identityProvider = identityProvider;
	}

	public void handle(UserId id) {
		User user = Users.require(identityProvider, id);
		if (!user.canSignIn()) {
			throw new InvalidStateException("A disabled or locked user cannot reset their password");
		}
		identityProvider.sendPasswordResetEmail(id);
	}
}
