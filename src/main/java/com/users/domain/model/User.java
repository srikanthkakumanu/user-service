package com.users.domain.model;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.users.domain.exception.InvalidStateException;

/**
 * The user as an identity: who they are and whether the account can be used. The identity
 * provider is the source of truth for this aggregate; the extended {@link UserProfile} lives in
 * this service's own database.
 */
public final class User {

	private final UserId id;
	private final Username username;
	private Email email;
	private PersonName name;
	private boolean emailVerified;
	private AccountStatus status;
	private final Set<RequiredAction> requiredActions;
	private final Instant createdAt;

	private User(UserId id, Username username, Email email, PersonName name, boolean emailVerified,
			AccountStatus status, Set<RequiredAction> requiredActions, Instant createdAt) {
		this.id = Objects.requireNonNull(id, "id");
		this.username = Objects.requireNonNull(username, "username");
		this.email = Objects.requireNonNull(email, "email");
		this.name = Objects.requireNonNull(name, "name");
		this.emailVerified = emailVerified;
		this.status = Objects.requireNonNull(status, "status");
		this.requiredActions = requiredActions.isEmpty() ? EnumSet.noneOf(RequiredAction.class)
				: EnumSet.copyOf(requiredActions);
		this.createdAt = createdAt;
	}

	/** Rebuilds a user from what the identity provider holds. */
	public static User rehydrate(UserId id, Username username, Email email, PersonName name, boolean emailVerified,
			AccountStatus status, Set<RequiredAction> requiredActions, Instant createdAt) {
		return new User(id, username, email, name, emailVerified, status, requiredActions, createdAt);
	}

	/** Changing the email means it has to be verified again. */
	public void changeEmail(Email newEmail) {
		Objects.requireNonNull(newEmail, "newEmail");
		if (!newEmail.equals(email)) {
			this.email = newEmail;
			this.emailVerified = false;
		}
	}

	public void rename(PersonName newName) {
		this.name = Objects.requireNonNull(newName, "newName");
	}

	public void disable() {
		if (status == AccountStatus.LOCKED) {
			throw new InvalidStateException("A locked user must be unlocked before it can be disabled");
		}
		this.status = AccountStatus.DISABLED;
	}

	public void enable() {
		if (status == AccountStatus.LOCKED) {
			throw new InvalidStateException("A locked user must be unlocked, not enabled");
		}
		this.status = AccountStatus.ACTIVE;
	}

	public void lock() {
		if (status == AccountStatus.DISABLED) {
			throw new InvalidStateException("A disabled user cannot be locked");
		}
		this.status = AccountStatus.LOCKED;
	}

	public void unlock() {
		if (status != AccountStatus.LOCKED) {
			throw new InvalidStateException("The user is not locked");
		}
		this.status = AccountStatus.ACTIVE;
	}

	public void require(Set<RequiredAction> actions) {
		requiredActions.clear();
		requiredActions.addAll(Objects.requireNonNull(actions, "actions"));
	}

	public boolean canSignIn() {
		return status == AccountStatus.ACTIVE;
	}

	public UserId id() {
		return id;
	}

	public Username username() {
		return username;
	}

	public Email email() {
		return email;
	}

	public PersonName name() {
		return name;
	}

	public boolean emailVerified() {
		return emailVerified;
	}

	public AccountStatus status() {
		return status;
	}

	public Set<RequiredAction> requiredActions() {
		return Set.copyOf(requiredActions);
	}

	public Instant createdAt() {
		return createdAt;
	}
}
