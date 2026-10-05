package com.users.domain.model;

import java.util.Set;

/** Whoever is asking for an operation, as far as domain guards need to know. */
public record Actor(UserId id, Set<String> roles) {

	public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";

	public Actor {
		roles = Set.copyOf(roles);
	}

	public boolean isPlatformAdmin() {
		return roles.contains(PLATFORM_ADMIN);
	}

	public boolean is(UserId other) {
		return id.equals(other);
	}
}
