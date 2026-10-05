package com.users.domain.event;

import java.time.Instant;

import com.users.domain.model.UserId;

/** Something that happened to a user. Published in-process today; shaped to go to a broker later. */
public sealed interface DomainEvent permits UserRegistered, UserCreated, UserUpdated, UserEnabled, UserDisabled,
		UserLocked, UserUnlocked, UserDeleted, UserProfileUpdated, UserPasswordReset {

	UserId userId();

	Instant occurredAt();

	default String type() {
		return getClass().getSimpleName();
	}
}
