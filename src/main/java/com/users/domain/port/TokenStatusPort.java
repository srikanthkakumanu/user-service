package com.users.domain.port;

/** Whether a token is still accepted by the identity provider right now. */
public interface TokenStatusPort {

	boolean isActive(String token);
}
