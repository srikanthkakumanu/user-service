package com.users.domain.exception;

/** The caller's token is no longer accepted by the identity provider. */
public class InvalidTokenException extends DomainException {

	public InvalidTokenException() {
		super("invalid-token", "The token is invalid or has expired");
	}
}
