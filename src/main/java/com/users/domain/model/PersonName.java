package com.users.domain.model;

import com.users.domain.exception.InvalidValueException;

/** A person's given and family name. */
public record PersonName(String firstName, String lastName) {

	private static final int MAX_LENGTH = 100;

	public PersonName {
		firstName = part("firstName", firstName);
		lastName = part("lastName", lastName);
	}

	public static PersonName of(String firstName, String lastName) {
		return new PersonName(firstName, lastName);
	}

	public String fullName() {
		return firstName + " " + lastName;
	}

	private static String part(String field, String value) {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException(field, field + " must not be blank");
		}
		String stripped = value.strip();
		if (stripped.length() > MAX_LENGTH) {
			throw new InvalidValueException(field, field + " must be at most " + MAX_LENGTH + " characters");
		}
		return stripped;
	}
}
