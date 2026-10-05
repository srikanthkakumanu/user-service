package com.users.domain.exception;

/** The identity provider rejected a password against the realm password policy. */
public class PasswordPolicyException extends DomainException {

	public PasswordPolicyException() {
		super("password-policy", "The password does not meet the password policy");
	}
}
