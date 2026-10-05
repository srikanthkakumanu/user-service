package com.users.application;

import com.users.domain.exception.DuplicateUserException;
import com.users.domain.exception.IdentityProviderUnavailableException;
import com.users.domain.exception.InvalidValueException;
import com.users.domain.model.AccountStatus;
import com.users.domain.model.RequiredAction;
import com.users.domain.model.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserCreationUseCasesTest extends UseCaseTest {

	private final RegisterUser register = new RegisterUser(identityProvider, profiles, this::publish, clock);
	private final CreateUser create = new CreateUser(identityProvider, profiles, this::publish, clock);

	@Test
	void registrationCreatesIdentityAndProfileAndSendsVerificationEmail() {
		UserId id = register.handle(new RegisterUser.Command("Alice", "Alice@Example.com", "Alice", "Doe", "S3cret!Passw0rd"));

		var created = identityProvider.created.get(id);
		assertThat(created.username().value()).isEqualTo("alice");
		assertThat(created.email().value()).isEqualTo("alice@example.com");
		assertThat(created.temporaryPassword()).isFalse();
		assertThat(created.requiredActions()).containsExactly(RequiredAction.VERIFY_EMAIL);
		assertThat(profiles.profiles).containsKey(id);
		assertThat(profiles.profiles.get(id).createdAt()).isEqualTo(NOW);
		assertThat(identityProvider.verificationEmails).containsExactly(id);
		assertThat(publishedTypes()).containsExactly("UserRegistered");
	}

	@Test
	void registrationRejectsInvalidInputBeforeTouchingAnySystem() {
		assertThatExceptionOfType(InvalidValueException.class).isThrownBy(
				() -> register.handle(new RegisterUser.Command("alice", "not-an-email", "Alice", "Doe", "S3cret!Passw0rd")));

		assertThat(identityProvider.users).isEmpty();
		assertThat(profiles.profiles).isEmpty();
		assertThat(published).isEmpty();
	}

	@Test
	void duplicateUsernameOrEmailIsAConflictAndCreatesNothing() {
		identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		assertThatExceptionOfType(DuplicateUserException.class).isThrownBy(
				() -> register.handle(new RegisterUser.Command("alice", "other@example.com", "Alice", "Doe", "S3cret!Passw0rd")))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("duplicate-user"));
		assertThatExceptionOfType(DuplicateUserException.class).isThrownBy(
				() -> register.handle(new RegisterUser.Command("alice2", "alice@example.com", "Alice", "Doe", "S3cret!Passw0rd")));

		assertThat(identityProvider.users).hasSize(1);
		assertThat(profiles.profiles).isEmpty();
	}

	@Test
	void failedProfileWriteDeletesTheIdentityAgain() {
		var failure = new IllegalStateException("database down");
		profiles.failOnSave = failure;

		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
				() -> register.handle(new RegisterUser.Command("alice", "alice@example.com", "Alice", "Doe", "S3cret!Passw0rd")))
				.isSameAs(failure);

		assertThat(identityProvider.users).isEmpty();
		assertThat(identityProvider.deleted).hasSize(1);
		assertThat(published).isEmpty();
	}

	@Test
	void failedVerificationEmailRollsBackIdentityAndProfile() {
		identityProvider.failOnVerificationEmail = new IdentityProviderUnavailableException("mail down", null);

		assertThatExceptionOfType(IdentityProviderUnavailableException.class).isThrownBy(
				() -> register.handle(new RegisterUser.Command("alice", "alice@example.com", "Alice", "Doe", "S3cret!Passw0rd")));

		assertThat(identityProvider.users).isEmpty();
		assertThat(profiles.profiles).isEmpty();
	}

	@Test
	void aFailingCompensationIsReportedAlongsideTheOriginalFailure() {
		var failure = new IllegalStateException("database down");
		profiles.failOnSave = failure;
		identityProvider.failOnDelete = new IllegalStateException("identity provider down");

		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
				() -> register.handle(new RegisterUser.Command("alice", "alice@example.com", "Alice", "Doe", "S3cret!Passw0rd")))
				.isSameAs(failure)
				.satisfies(ex -> assertThat(ex.getSuppressed()).hasSize(1));
	}

	@Test
	void adminCreationWithPasswordMakesItTemporary() {
		UserId id = create.handle(new CreateUser.Command("bob", "bob@example.com", "Bob", "Roe", "Temp!Passw0rd1", true));

		var created = identityProvider.created.get(id);
		assertThat(created.temporaryPassword()).isTrue();
		assertThat(created.emailVerified()).isTrue();
		assertThat(created.requiredActions()).containsExactly(RequiredAction.UPDATE_PASSWORD);
		assertThat(profiles.profiles).containsKey(id);
		assertThat(identityProvider.verificationEmails).isEmpty();
		assertThat(publishedTypes()).containsExactly("UserCreated");
	}

	@Test
	void adminCreationWithoutPasswordLeavesTheUserToVerifyEmail() {
		UserId id = create.handle(new CreateUser.Command("bob", "bob@example.com", "Bob", "Roe", null, false));

		assertThat(identityProvider.created.get(id).password()).isNull();
		assertThat(identityProvider.created.get(id).requiredActions()).containsExactly(RequiredAction.VERIFY_EMAIL);
	}

	@Test
	void adminCreationIsCompensatedToo() {
		profiles.failOnSave = new IllegalStateException("database down");

		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
				() -> create.handle(new CreateUser.Command("bob", "bob@example.com", "Bob", "Roe", null, false)));

		assertThat(identityProvider.users).isEmpty();
	}
}
