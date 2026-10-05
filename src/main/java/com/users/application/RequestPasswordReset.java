package com.users.application;

import com.users.domain.model.Email;
import com.users.domain.model.User;
import com.users.domain.port.IdentityProviderPort;

/**
 * Anyone may ask for a password-reset email. The outcome is the same whether or not the address
 * belongs to an account, so the endpoint cannot be used to find out who is registered.
 */
public final class RequestPasswordReset {

	private final IdentityProviderPort identityProvider;

	public RequestPasswordReset(IdentityProviderPort identityProvider) {
		this.identityProvider = identityProvider;
	}

	public void handle(String email) {
		identityProvider.findByEmail(Email.of(email)).filter(User::canSignIn)
				.ifPresent(user -> identityProvider.sendPasswordResetEmail(user.id()));
	}
}
