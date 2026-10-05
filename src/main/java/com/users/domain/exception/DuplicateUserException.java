package com.users.domain.exception;

/** The username or email is already taken. Which one is deliberately not disclosed. */
public class DuplicateUserException extends DomainException {

	public DuplicateUserException() {
		super("duplicate-user", "A user with this username or email already exists");
	}
}
