package com.users.domain.model;

import com.users.domain.exception.InvalidValueException;

/** Filter and paging for listing users. Results are always ordered by username. */
public record UserSearch(String query, Boolean enabled, int page, int size) {

	public static final int MAX_SIZE = 100;

	public UserSearch {
		if (page < 0) {
			throw new InvalidValueException("page", "page must not be negative");
		}
		if (size < 1 || size > MAX_SIZE) {
			throw new InvalidValueException("size", "size must be between 1 and " + MAX_SIZE);
		}
		query = query == null || query.isBlank() ? null : query.strip();
	}

	public int offset() {
		return page * size;
	}
}
