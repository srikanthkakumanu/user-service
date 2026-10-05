package com.users.domain.model;

import java.time.Instant;

import com.users.domain.exception.InvalidValueException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserProfileTest {

	private static final UserId ID = UserId.of("7c1f0e9a-3b5d-4f6a-8b7c-9d0e1f2a3b4c");
	private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant LATER = Instant.parse("2026-02-01T00:00:00Z");

	@Test
	void startsEmpty() {
		var profile = UserProfile.empty(ID, CREATED);

		assertThat(profile.userId()).isEqualTo(ID);
		assertThat(profile.phoneNumber()).isNull();
		assertThat(profile.createdAt()).isEqualTo(CREATED);
		assertThat(profile.updatedAt()).isEqualTo(CREATED);
	}

	@Test
	void updateReplacesEveryAttributeAndStampsTheChange() {
		var profile = UserProfile.empty(ID, CREATED);

		profile.update(" +44 20 7946 0958 ", " Engineer ", "Platform", "en-GB", "Europe/London", "Hello", LATER);

		assertThat(profile.phoneNumber()).isEqualTo("+44 20 7946 0958");
		assertThat(profile.jobTitle()).isEqualTo("Engineer");
		assertThat(profile.department()).isEqualTo("Platform");
		assertThat(profile.locale()).isEqualTo("en-GB");
		assertThat(profile.timeZone()).isEqualTo("Europe/London");
		assertThat(profile.bio()).isEqualTo("Hello");
		assertThat(profile.createdAt()).isEqualTo(CREATED);
		assertThat(profile.updatedAt()).isEqualTo(LATER);
	}

	@Test
	void blankValuesClearAttributes() {
		var profile = UserProfile.rehydrate(ID, "+44 20 7946 0958", "Engineer", "Platform", "en", "UTC", "Hi",
				CREATED, CREATED);

		profile.update(" ", null, "", null, null, null, LATER);

		assertThat(profile.phoneNumber()).isNull();
		assertThat(profile.jobTitle()).isNull();
		assertThat(profile.department()).isNull();
		assertThat(profile.bio()).isNull();
	}

	@Test
	void rejectsInvalidAttributesAndLeavesTheProfileUnchanged() {
		var profile = UserProfile.rehydrate(ID, null, "Engineer", null, null, null, null, CREATED, CREATED);

		assertInvalid(() -> profile.update("call me", null, null, null, null, null, LATER), "phoneNumber");
		assertInvalid(() -> profile.update(null, null, null, null, "Mars/Olympus", null, LATER), "timeZone");
		assertInvalid(() -> profile.update(null, null, null, "!!", null, null, LATER), "locale");
		assertInvalid(() -> profile.update(null, "x".repeat(101), null, null, null, null, LATER), "jobTitle");
		assertInvalid(() -> profile.update(null, null, null, null, null, "x".repeat(1001), LATER), "bio");
		assertThat(profile.jobTitle()).isEqualTo("Engineer");
		assertThat(profile.updatedAt()).isEqualTo(CREATED);
	}

	private static void assertInvalid(Runnable action, String field) {
		assertThatExceptionOfType(InvalidValueException.class).isThrownBy(action::run)
				.satisfies(ex -> assertThat(ex.field()).isEqualTo(field));
	}
}
