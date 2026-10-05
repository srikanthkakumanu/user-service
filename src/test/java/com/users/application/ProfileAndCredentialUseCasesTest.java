package com.users.application;

import com.users.domain.exception.InvalidStateException;
import com.users.domain.exception.InvalidValueException;
import com.users.domain.exception.OperationNotPermittedException;
import com.users.domain.exception.UserNotFoundException;
import com.users.domain.model.AccountStatus;
import com.users.domain.model.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ProfileAndCredentialUseCasesTest extends UseCaseTest {

	private final GetProfile getProfile = new GetProfile(identityProvider, profiles, clock);
	private final UpdateProfile updateProfile = new UpdateProfile(identityProvider, profiles, this::publish, clock);

	@Test
	void aUserWithoutAStoredProfileGetsAnEmptyOne() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		var result = getProfile.handle(id);

		assertThat(result.user().username().value()).isEqualTo("alice");
		assertThat(result.profile().jobTitle()).isNull();
		assertThat(profiles.profiles).isEmpty();
	}

	@Test
	void updateStoresProfileAndRenamesTheIdentity() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		var result = updateProfile.handle(new UpdateProfile.Command(id, "Alicia", "Roe", "+44 20 7946 0958",
				"Engineer", "Platform", "en-GB", "Europe/London", "Hello"));

		assertThat(result.user().name().fullName()).isEqualTo("Alicia Roe");
		assertThat(identityProvider.users.get(id).name().fullName()).isEqualTo("Alicia Roe");
		assertThat(profiles.profiles.get(id).jobTitle()).isEqualTo("Engineer");
		assertThat(profiles.profiles.get(id).updatedAt()).isEqualTo(NOW);
		assertThat(publishedTypes()).containsExactly("UserProfileUpdated");
	}

	@Test
	void anInvalidProfileChangesNeitherSystem() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		assertThatExceptionOfType(InvalidValueException.class).isThrownBy(() -> updateProfile.handle(
				new UpdateProfile.Command(id, "Alicia", "Roe", null, null, null, null, "Mars/Olympus", null)));

		assertThat(identityProvider.users.get(id).name().fullName()).isEqualTo("Test User");
		assertThat(profiles.profiles).isEmpty();
		assertThat(published).isEmpty();
	}

	@Test
	void profileOfAnUnknownUserIsNotFound() {
		var unknown = UserId.of("99999999-9999-9999-9999-999999999999");

		assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> getProfile.handle(unknown));
	}

	@Test
	void verificationEmailIsOnlySentWhileUnverified() {
		UserId unverified = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, false);
		UserId verified = identityProvider.add("bob", "bob@example.com", AccountStatus.ACTIVE, true);
		var send = new SendVerificationEmail(identityProvider);

		send.handle(unverified);

		assertThat(identityProvider.verificationEmails).containsExactly(unverified);
		assertThatExceptionOfType(InvalidStateException.class).isThrownBy(() -> send.handle(verified));
	}

	@Test
	void adminPasswordResetEmailRequiresAnAccountThatCanSignIn() {
		UserId active = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);
		UserId disabled = identityProvider.add("bob", "bob@example.com", AccountStatus.DISABLED, true);
		var send = new SendPasswordResetEmail(identityProvider);

		send.handle(active);

		assertThat(identityProvider.resetEmails).containsExactly(active);
		assertThatExceptionOfType(InvalidStateException.class).isThrownBy(() -> send.handle(disabled));
	}

	@Test
	void publicPasswordResetRequestNeverRevealsWhetherAnAccountExists() {
		UserId active = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);
		identityProvider.add("bob", "bob@example.com", AccountStatus.LOCKED, true);
		var request = new RequestPasswordReset(identityProvider);

		request.handle("ALICE@example.com");
		request.handle("bob@example.com");
		request.handle("nobody@example.com");

		assertThat(identityProvider.resetEmails).containsExactly(active);
		assertThatExceptionOfType(InvalidValueException.class).isThrownBy(() -> request.handle("not-an-email"));
	}

	@Test
	void adminPasswordResetSetsThePasswordAndEndsSessions() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		new ResetPassword(identityProvider, this::publish, clock).handle(userAdmin, id, "Temp!Passw0rd1", true);

		assertThat(identityProvider.passwords).containsEntry(id, "Temp!Passw0rd1");
		assertThat(identityProvider.temporaryPasswords).containsEntry(id, true);
		assertThat(identityProvider.signedOut).containsExactly(id);
		assertThat(publishedTypes()).containsExactly("UserPasswordReset");
	}

	@Test
	void aUserAdminCannotResetAPlatformAdminsPasswordOrRemoveTheirCredentials() {
		UserId admin = identityProvider.add("root", "root@example.com", AccountStatus.ACTIVE, true);
		identityProvider.platformAdmins.add(admin);

		assertThatExceptionOfType(OperationNotPermittedException.class).isThrownBy(
				() -> new ResetPassword(identityProvider, this::publish, clock).handle(userAdmin, admin, "Temp!Passw0rd1", true));
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> new RemoveCredential(identityProvider).handle(userAdmin, admin, "cred-1"));
		assertThat(identityProvider.passwords).isEmpty();
		assertThat(identityProvider.removedCredentials).isEmpty();
	}

	@Test
	void aPlatformAdminMayResetAnotherPlatformAdminsPassword() {
		UserId admin = identityProvider.add("root", "root@example.com", AccountStatus.ACTIVE, true);
		identityProvider.platformAdmins.add(admin);

		new ResetPassword(identityProvider, this::publish, clock).handle(platformAdmin, admin, "Temp!Passw0rd1", false);

		assertThat(identityProvider.temporaryPasswords).containsEntry(admin, false);
	}

	@Test
	void listsAndRemovesCredentials() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		assertThat(new ListCredentials(identityProvider).handle(id)).extracting("type").containsExactly("password");
		new RemoveCredential(identityProvider).handle(userAdmin, id, "cred-1");

		assertThat(identityProvider.removedCredentials).containsExactly("cred-1");
		assertThat(identityProvider.signedOut).containsExactly(id);
	}
}
