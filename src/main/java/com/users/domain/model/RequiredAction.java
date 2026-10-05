package com.users.domain.model;

/** Something the user must do at next sign-in before the account is fully usable. */
public enum RequiredAction {
	VERIFY_EMAIL, UPDATE_PASSWORD, UPDATE_PROFILE
}
