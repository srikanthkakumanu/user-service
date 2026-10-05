package com.users.domain.model;

/**
 * Account state. A disabled account was switched off, typically for good or pending review.
 * A locked account was suspended by an administrator and must be explicitly unlocked.
 * Neither can sign in.
 */
public enum AccountStatus {
	ACTIVE, DISABLED, LOCKED
}
