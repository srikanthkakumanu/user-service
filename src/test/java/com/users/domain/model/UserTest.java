package com.users.domain.model;

import java.time.Instant;
import java.util.Set;

import com.users.domain.exception.InvalidStateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserTest {

	@Test
	void changingTheEmailRequiresVerifyingItAgain() {
		User user = user(AccountStatus.ACTIVE, true);

		user.changeEmail(Email.of("new@example.com"));

		assertThat(user.email().value()).isEqualTo("new@example.com");
		assertThat(user.emailVerified()).isFalse();
	}

	@Test
	void keepingTheSameEmailKeepsItVerified() {
		User user = user(AccountStatus.ACTIVE, true);

		user.changeEmail(Email.of("ALICE@example.com"));

		assertThat(user.emailVerified()).isTrue();
	}

	@Test
	void renames() {
		User user = user(AccountStatus.ACTIVE, true);

		user.rename(PersonName.of("Alicia", "Roe"));

		assertThat(user.name().fullName()).isEqualTo("Alicia Roe");
	}

	@Test
	void disableAndEnableSwitchSignInOffAndOn() {
		User user = user(AccountStatus.ACTIVE, true);

		user.disable();
		assertThat(user.status()).isEqualTo(AccountStatus.DISABLED);
		assertThat(user.canSignIn()).isFalse();

		user.enable();
		assertThat(user.status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(user.canSignIn()).isTrue();
	}

	@Test
	void lockAndUnlock() {
		User user = user(AccountStatus.ACTIVE, true);

		user.lock();
		assertThat(user.status()).isEqualTo(AccountStatus.LOCKED);
		assertThat(user.canSignIn()).isFalse();

		user.unlock();
		assertThat(user.status()).isEqualTo(AccountStatus.ACTIVE);
	}

	@Test
	void aLockedUserMustBeUnlockedNotEnabledOrDisabled() {
		User user = user(AccountStatus.LOCKED, true);

		assertThatExceptionOfType(InvalidStateException.class).isThrownBy(user::enable);
		assertThatExceptionOfType(InvalidStateException.class).isThrownBy(user::disable);
		assertThat(user.status()).isEqualTo(AccountStatus.LOCKED);
	}

	@Test
	void aDisabledUserCannotBeLockedAndOnlyLockedUsersCanBeUnlocked() {
		assertThatExceptionOfType(InvalidStateException.class).isThrownBy(user(AccountStatus.DISABLED, true)::lock);
		assertThatExceptionOfType(InvalidStateException.class).isThrownBy(user(AccountStatus.ACTIVE, true)::unlock)
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("invalid-state"));
	}

	@Test
	void requiredActionsAreReplacedAndNotExposedForMutation() {
		User user = user(AccountStatus.ACTIVE, false);
		assertThat(user.requiredActions()).containsExactly(RequiredAction.VERIFY_EMAIL);

		user.require(Set.of(RequiredAction.UPDATE_PASSWORD, RequiredAction.UPDATE_PROFILE));

		assertThat(user.requiredActions()).containsExactlyInAnyOrder(RequiredAction.UPDATE_PASSWORD,
				RequiredAction.UPDATE_PROFILE);
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> user.requiredActions().add(RequiredAction.VERIFY_EMAIL));
	}

	@Test
	void selfRegisteredUserKeepsOwnPasswordAndMustVerifyEmail() {
		var newUser = NewUser.selfRegistered(Username.of("alice"), Email.of("alice@example.com"),
				PersonName.of("Alice", "Doe"), Password.of("S3cret!Passw0rd"));

		assertThat(newUser.temporaryPassword()).isFalse();
		assertThat(newUser.emailVerified()).isFalse();
		assertThat(newUser.requiredActions()).containsExactly(RequiredAction.VERIFY_EMAIL);
	}

	@Test
	void adminCreatedUserGetsATemporaryPasswordThatMustBeChanged() {
		var withPassword = NewUser.createdByAdmin(Username.of("bob"), Email.of("bob@example.com"),
				PersonName.of("Bob", "Roe"), Password.of("Temp!Passw0rd1"), true);
		var withoutPassword = NewUser.createdByAdmin(Username.of("bob"), Email.of("bob@example.com"),
				PersonName.of("Bob", "Roe"), null, false);

		assertThat(withPassword.temporaryPassword()).isTrue();
		assertThat(withPassword.requiredActions()).containsExactly(RequiredAction.UPDATE_PASSWORD);
		assertThat(withoutPassword.password()).isNull();
		assertThat(withoutPassword.requiredActions()).containsExactly(RequiredAction.VERIFY_EMAIL);
	}

	static User user(AccountStatus status, boolean emailVerified) {
		return User.rehydrate(UserId.of("7c1f0e9a-3b5d-4f6a-8b7c-9d0e1f2a3b4c"), Username.of("alice"),
				Email.of("alice@example.com"), PersonName.of("Alice", "Doe"), emailVerified, status,
				emailVerified ? Set.of() : Set.of(RequiredAction.VERIFY_EMAIL), Instant.parse("2026-01-01T00:00:00Z"));
	}
}
