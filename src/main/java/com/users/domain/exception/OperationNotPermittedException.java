package com.users.domain.exception;

/** A domain guard refused the operation, whatever permissions the actor holds. */
public class OperationNotPermittedException extends DomainException {

	public OperationNotPermittedException(String message) {
		super("operation-not-permitted", message);
	}
}
