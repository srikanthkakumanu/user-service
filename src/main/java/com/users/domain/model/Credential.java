package com.users.domain.model;

import java.time.Instant;

/** A credential a user holds, described without its secret. */
public record Credential(String id, String type, String label, Instant createdAt) {
}
