package com.users.domain.model;

import com.users.domain.exception.InvalidValueException;

/**
 * A password on its way to the identity provider. Strength is the realm policy's job; this only
 * rules out empty and absurdly long values and keeps the secret out of logs.
 */
public record Password(String value) {

	private static final int MAX_LENGTH = 256;

	public Password {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException("password", "Password must not be blank");
		}
		if (value.length() > MAX_LENGTH) {
			throw new InvalidValueException("password", "Password must be at most " + MAX_LENGTH + " characters");
		}
	}

	public static Password of(String value) {
		return new Password(value);
	}

	@Override
	public String toString() {
		return "Password[***]";
	}
}
