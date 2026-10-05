package com.users.interfaces.rest.dto;

import java.util.Set;

import com.users.domain.model.RequiredAction;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request bodies of the user API. */
public final class Requests {

	private Requests() {
	}

	public record Register(
			@NotBlank @Size(min = 3, max = 50) String username,
			@NotBlank @Email @Size(max = 254) String email,
			@NotBlank @Size(max = 100) String firstName,
			@NotBlank @Size(max = 100) String lastName,
			@NotBlank @Size(max = 256) String password) {
	}

	public record CreateUser(
			@NotBlank @Size(min = 3, max = 50) String username,
			@NotBlank @Email @Size(max = 254) String email,
			@NotBlank @Size(max = 100) String firstName,
			@NotBlank @Size(max = 100) String lastName,
			@Size(max = 256) String temporaryPassword,
			boolean emailVerified) {
	}

	public record UpdateUser(
			@NotBlank @Email @Size(max = 254) String email,
			@NotBlank @Size(max = 100) String firstName,
			@NotBlank @Size(max = 100) String lastName) {
	}

	public record UpdateProfile(
			@NotBlank @Size(max = 100) String firstName,
			@NotBlank @Size(max = 100) String lastName,
			@Size(max = 30) String phoneNumber,
			@Size(max = 100) String jobTitle,
			@Size(max = 100) String department,
			@Size(max = 35) String locale,
			@Size(max = 100) String timeZone,
			@Size(max = 1000) String bio) {
	}

	public record PasswordResetRequest(@NotBlank @Email @Size(max = 254) String email) {
	}

	public record SetRequiredActions(@NotNull Set<RequiredAction> actions) {
	}

	public record SetPassword(@NotBlank @Size(max = 256) String password, boolean temporary) {
	}
}
