package com.users.domain.exception;

/** The operation does not apply to the user's current account state. */
public class InvalidStateException extends DomainException {

	public InvalidStateException(String message) {
		super("invalid-state", message);
	}
}
