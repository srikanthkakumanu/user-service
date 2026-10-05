package com.users.interfaces.security;

/** Method-security expressions used by the controllers. {@code authentication.name} is the token subject. */
public final class Permissions {

	public static final String READ = "hasAuthority('users:read')";
	public static final String WRITE = "hasAuthority('users:write')";
	public static final String READ_OR_SELF = "hasAuthority('users:read') or #id == authentication.name";
	public static final String WRITE_OR_SELF = "hasAuthority('users:write') or #id == authentication.name";

	private Permissions() {
	}
}
