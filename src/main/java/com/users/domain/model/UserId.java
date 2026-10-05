package com.users.domain.model;

import java.util.UUID;

import com.users.domain.exception.InvalidValueException;

/** Identity of a user; the identity provider's user ID and the token {@code sub}. */
public record UserId(String value) {

	public UserId {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException("id", "User id must not be blank");
		}
		try {
			value = UUID.fromString(value.strip()).toString();
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidValueException("id", "User id must be a UUID");
		}
	}

	public static UserId of(String value) {
		return new UserId(value);
	}

	@Override
	public String toString() {
		return value;
	}
}
