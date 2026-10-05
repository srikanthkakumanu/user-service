package com.users.domain.exception;

import com.users.domain.model.UserId;

public class UserNotFoundException extends DomainException {

	public UserNotFoundException(UserId id) {
		super("user-not-found", "No user with id " + id.value());
	}
}
