package com.users.application;

import com.users.domain.exception.InvalidTokenException;
import com.users.domain.port.TokenStatusPort;

/**
 * Asks the identity provider whether the caller's token is still good. A signature check alone
 * accepts a token until it expires; this also catches one whose session was ended or whose user
 * was disabled a moment ago. Used for sensitive operations only, because it costs a round trip.
 */
public final class EnsureTokenActive {

	private final TokenStatusPort tokens;

	public EnsureTokenActive(TokenStatusPort tokens) {
		this.tokens = tokens;
	}

	public void handle(String token) {
		if (!tokens.isActive(token)) {
			throw new InvalidTokenException();
		}
	}
}
