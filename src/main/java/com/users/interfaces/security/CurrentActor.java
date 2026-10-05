package com.users.interfaces.security;

import com.platform.security.claims.AccessTokenClaims;
import com.users.domain.model.Actor;
import com.users.domain.model.UserId;
import org.springframework.security.oauth2.jwt.Jwt;

/** Builds the domain's view of the caller from the validated access token. */
public final class CurrentActor {

	private CurrentActor() {
	}

	public static Actor from(Jwt jwt) {
		var claims = AccessTokenClaims.from(jwt.getClaims());
		return new Actor(UserId.of(claims.subject()), claims.realmRoles());
	}
}
