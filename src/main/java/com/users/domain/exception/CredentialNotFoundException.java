package com.users.domain.exception;

public class CredentialNotFoundException extends DomainException {

	public CredentialNotFoundException(String credentialId) {
		super("credential-not-found", "No credential with id " + credentialId);
	}
}
