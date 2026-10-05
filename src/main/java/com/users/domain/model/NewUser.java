package com.users.domain.model;

import java.util.Objects;
import java.util.Set;

/**
 * Everything needed to create an identity. {@code password} may be absent for an admin-created
 * user who is sent a set-password email instead.
 */
public record NewUser(Username username, Email email, PersonName name, Password password, boolean temporaryPassword,
		boolean emailVerified, Set<RequiredAction> requiredActions) {

	public NewUser {
		Objects.requireNonNull(username, "username");
		Objects.requireNonNull(email, "email");
		Objects.requireNonNull(name, "name");
		requiredActions = Set.copyOf(requiredActions);
	}

	/** A person signing themselves up: own password, email still to be verified. */
	public static NewUser selfRegistered(Username username, Email email, PersonName name, Password password) {
		Objects.requireNonNull(password, "password");
		return new NewUser(username, email, name, password, false, false, Set.of(RequiredAction.VERIFY_EMAIL));
	}

	/** An administrator creating an account; a supplied password is temporary and must be changed. */
	public static NewUser createdByAdmin(Username username, Email email, PersonName name, Password password,
			boolean emailVerified) {
		var actions = new java.util.HashSet<RequiredAction>();
		if (password != null) {
			actions.add(RequiredAction.UPDATE_PASSWORD);
		}
		if (!emailVerified) {
			actions.add(RequiredAction.VERIFY_EMAIL);
		}
		return new NewUser(username, email, name, password, password != null, emailVerified, actions);
	}
}
