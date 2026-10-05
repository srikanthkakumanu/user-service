package com.users.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

import com.users.domain.exception.InvalidValueException;

/** Email address, normalised to lower case. */
public record Email(String value) {

	private static final int MAX_LENGTH = 254;
	private static final Pattern PATTERN = Pattern
			.compile("[a-z0-9._%+-]+@[a-z0-9](?:[a-z0-9-]*[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)+");

	public Email {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException("email", "Email must not be blank");
		}
		value = value.strip().toLowerCase(Locale.ROOT);
		if (value.length() > MAX_LENGTH || !PATTERN.matcher(value).matches()) {
			throw new InvalidValueException("email", "Email is not a valid address");
		}
	}

	public static Email of(String value) {
		return new Email(value);
	}

	@Override
	public String toString() {
		return value;
	}
}
