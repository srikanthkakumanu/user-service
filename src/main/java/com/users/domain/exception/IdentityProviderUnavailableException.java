package com.users.domain.exception;

/** The identity provider failed or could not be reached. Details stay in the logs. */
public class IdentityProviderUnavailableException extends DomainException {

	public IdentityProviderUnavailableException(String message, Throwable cause) {
		super("identity-provider-unavailable", message, cause);
	}
}
