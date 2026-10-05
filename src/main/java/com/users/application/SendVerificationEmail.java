package com.users.application;

import com.users.domain.exception.InvalidStateException;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.port.IdentityProviderPort;

public final class SendVerificationEmail {

	private final IdentityProviderPort identityProvider;

	public SendVerificationEmail(IdentityProviderPort identityProvider) {
		this.identityProvider = identityProvider;
	}

	public void handle(UserId id) {
		User user = Users.require(identityProvider, id);
		if (user.emailVerified()) {
			throw new InvalidStateException("The email address is already verified");
		}
		identityProvider.sendVerificationEmail(id);
	}
}
