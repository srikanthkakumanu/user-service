package com.users.interfaces.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.users.application.GetProfile;
import com.users.domain.model.AccountStatus;
import com.users.domain.model.Credential;
import com.users.domain.model.PageResult;
import com.users.domain.model.RequiredAction;
import com.users.domain.model.User;

/** Response bodies of the user API. */
public final class Responses {

	private Responses() {
	}

	public record Created(String id) {
	}

	public record UserResponse(String id, String username, String email, boolean emailVerified, String firstName,
			String lastName, AccountStatus status, Set<RequiredAction> requiredActions, Instant createdAt) {

		public static UserResponse from(User user) {
			return new UserResponse(user.id().value(), user.username().value(), user.email().value(),
					user.emailVerified(), user.name().firstName(), user.name().lastName(), user.status(),
					user.requiredActions(), user.createdAt());
		}
	}

	public record ProfileResponse(String id, String username, String email, boolean emailVerified, String firstName,
			String lastName, String phoneNumber, String jobTitle, String department, String locale, String timeZone,
			String bio, Instant updatedAt) {

		public static ProfileResponse from(GetProfile.Result result) {
			var user = result.user();
			var profile = result.profile();
			return new ProfileResponse(user.id().value(), user.username().value(), user.email().value(),
					user.emailVerified(), user.name().firstName(), user.name().lastName(), profile.phoneNumber(),
					profile.jobTitle(), profile.department(), profile.locale(), profile.timeZone(), profile.bio(),
					profile.updatedAt());
		}
	}

	public record CredentialResponse(String id, String type, String label, Instant createdAt) {

		public static CredentialResponse from(Credential credential) {
			return new CredentialResponse(credential.id(), credential.type(), credential.label(),
					credential.createdAt());
		}
	}

	public record PageResponse<T>(List<T> items, long total, int page, int size) {

		public static <T> PageResponse<T> from(PageResult<T> page) {
			return new PageResponse<>(page.items(), page.total(), page.page(), page.size());
		}
	}
}
