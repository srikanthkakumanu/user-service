package com.users.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

import com.users.domain.exception.InvalidValueException;

/** Login name: 3 to 50 lower-case letters, digits, dots, hyphens or underscores, starting with a letter or digit. */
public record Username(String value) {

	private static final Pattern PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]{2,49}");

	public Username {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException("username", "Username must not be blank");
		}
		value = value.strip().toLowerCase(Locale.ROOT);
		if (!PATTERN.matcher(value).matches()) {
			throw new InvalidValueException("username",
					"Username must be 3 to 50 characters: letters, digits, '.', '-' or '_', starting with a letter or digit");
		}
	}

	public static Username of(String value) {
		return new Username(value);
	}

	@Override
	public String toString() {
		return value;
	}
}
