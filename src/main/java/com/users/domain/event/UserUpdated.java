package com.users.domain.event;

import java.time.Instant;

import com.users.domain.model.UserId;

public record UserUpdated(UserId userId, Instant occurredAt) implements DomainEvent {
}
