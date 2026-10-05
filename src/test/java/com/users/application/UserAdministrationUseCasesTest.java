package com.users.application;

import java.util.Set;

import com.users.domain.exception.InvalidStateException;
import com.users.domain.exception.OperationNotPermittedException;
import com.users.domain.exception.UserNotFoundException;
import com.users.domain.model.AccountStatus;
import com.users.domain.model.Actor;
import com.users.domain.model.RequiredAction;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.model.UserProfile;
import com.users.domain.model.UserSearch;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserAdministrationUseCasesTest extends UseCaseTest {

	private static final UserId UNKNOWN = UserId.of("99999999-9999-9999-9999-999999999999");

	@Test
	void getsAndSearchesUsers() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		assertThat(new GetUser(identityProvider).handle(id).username().value()).isEqualTo("alice");
		assertThat(new SearchUsers(identityProvider).handle(new UserSearch(null, null, 0, 20)).total())
				.isEqualTo(1);
		assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> new GetUser(identityProvider).handle(UNKNOWN))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("user-not-found"));
	}

	@Test
	void updateChangesEmailAndNameAndResetsVerification() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		User updated = new UpdateUser(identityProvider, this::publish, clock).handle(userAdmin,
				new UpdateUser.Command(id, "alice@new.example.com", "Alicia", "Roe"));

		assertThat(updated.email().value()).isEqualTo("alice@new.example.com");
		assertThat(updated.emailVerified()).isFalse();
		assertThat(identityProvider.users.get(id).name().fullName()).isEqualTo("Alicia Roe");
		assertThat(publishedTypes()).containsExactly("UserUpdated");
	}

	@Test
	void aUserAdminCannotChangeAPlatformAdminsEmail() {
		UserId admin = platformAdminUser("root");

		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> new UpdateUser(identityProvider, this::publish, clock).handle(userAdmin,
						new UpdateUser.Command(admin, "attacker@example.com", "Root", "Admin")));

		assertThat(identityProvider.users.get(admin).email().value()).isEqualTo("root@example.com");
	}

	@Test
	void disableEndsSessionsAndEnableRestoresAccess() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		new DisableUser(identityProvider, this::publish, clock).handle(userAdmin, id);
		assertThat(identityProvider.users.get(id).status()).isEqualTo(AccountStatus.DISABLED);
		assertThat(identityProvider.signedOut).containsExactly(id);

		new EnableUser(identityProvider, this::publish, clock).handle(userAdmin, id);
		assertThat(identityProvider.users.get(id).status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(publishedTypes()).containsExactly("UserDisabled", "UserEnabled");
	}

	@Test
	void lockEndsSessionsAndOnlyUnlockLiftsIt() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		new LockUser(identityProvider, this::publish, clock).handle(userAdmin, id);
		assertThat(identityProvider.users.get(id).status()).isEqualTo(AccountStatus.LOCKED);
		assertThat(identityProvider.signedOut).containsExactly(id);
		assertThatExceptionOfType(InvalidStateException.class)
				.isThrownBy(() -> new EnableUser(identityProvider, this::publish, clock).handle(userAdmin, id));

		new UnlockUser(identityProvider, this::publish, clock).handle(userAdmin, id);
		assertThat(identityProvider.users.get(id).status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(publishedTypes()).containsExactly("UserLocked", "UserUnlocked");
	}

	@Test
	void nobodyDisablesLocksOrDeletesTheirOwnAccount() {
		UserId self = identityProvider.add("admin", "admin@example.com", AccountStatus.ACTIVE, true);
		var actor = new Actor(self, Set.of("USER_ADMIN"));

		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> new DisableUser(identityProvider, this::publish, clock).handle(actor, self));
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> new LockUser(identityProvider, this::publish, clock).handle(actor, self));
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> new DeleteUser(identityProvider, profiles, this::publish, clock).handle(actor, self));
		assertThat(identityProvider.users.get(self).status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(published).isEmpty();
	}

	@Test
	void theLastPlatformAdminCannotBeDisabledLockedOrDeleted() {
		UserId last = platformAdminUser("root");

		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> new DisableUser(identityProvider, this::publish, clock).handle(platformAdmin, last));
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> new LockUser(identityProvider, this::publish, clock).handle(platformAdmin, last));
		assertThatExceptionOfType(OperationNotPermittedException.class).isThrownBy(
				() -> new DeleteUser(identityProvider, profiles, this::publish, clock).handle(platformAdmin, last));
		assertThat(identityProvider.users).containsKey(last);
	}

	@Test
	void aPlatformAdminCanBeRemovedWhileAnotherRemains() {
		UserId first = platformAdminUser("root");
		platformAdminUser("second");

		new DisableUser(identityProvider, this::publish, clock).handle(platformAdmin, first);

		assertThat(identityProvider.users.get(first).status()).isEqualTo(AccountStatus.DISABLED);
	}

	@Test
	void aUserAdminCannotDisableOrDeleteAPlatformAdminEvenWhenOthersRemain() {
		UserId first = platformAdminUser("root");
		platformAdminUser("second");

		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> new DisableUser(identityProvider, this::publish, clock).handle(userAdmin, first));
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> new DeleteUser(identityProvider, profiles, this::publish, clock).handle(userAdmin, first));
	}

	@Test
	void deleteRemovesIdentityAndProfile() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);
		profiles.save(UserProfile.empty(id, NOW));

		new DeleteUser(identityProvider, profiles, this::publish, clock).handle(userAdmin, id);

		assertThat(identityProvider.users).doesNotContainKey(id);
		assertThat(profiles.profiles).doesNotContainKey(id);
		assertThat(publishedTypes()).containsExactly("UserDeleted");
	}

	@Test
	void aFailedIdentityDeletionKeepsTheProfile() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);
		profiles.save(UserProfile.empty(id, NOW));
		identityProvider.failOnDelete = new IllegalStateException("identity provider down");

		assertThatExceptionOfType(IllegalStateException.class)
				.isThrownBy(() -> new DeleteUser(identityProvider, profiles, this::publish, clock).handle(userAdmin, id));

		assertThat(profiles.profiles).containsKey(id);
		assertThat(published).isEmpty();
	}

	@Test
	void stateChangesOnUnknownUsersAreNotFound() {
		assertThatExceptionOfType(UserNotFoundException.class)
				.isThrownBy(() -> new DisableUser(identityProvider, this::publish, clock).handle(userAdmin, UNKNOWN));
		assertThatExceptionOfType(UserNotFoundException.class)
				.isThrownBy(() -> new DeleteUser(identityProvider, profiles, this::publish, clock).handle(userAdmin, UNKNOWN));
	}

	@Test
	void requiredActionsAreReplaced() {
		UserId id = identityProvider.add("alice", "alice@example.com", AccountStatus.ACTIVE, true);

		User user = new SetRequiredActions(identityProvider).handle(userAdmin, id,
				Set.of(RequiredAction.UPDATE_PASSWORD, RequiredAction.UPDATE_PROFILE));

		assertThat(user.requiredActions()).containsExactlyInAnyOrder(RequiredAction.UPDATE_PASSWORD,
				RequiredAction.UPDATE_PROFILE);
		assertThat(identityProvider.users.get(id).requiredActions()).hasSize(2);
	}

	private UserId platformAdminUser(String username) {
		UserId id = identityProvider.add(username, username + "@example.com", AccountStatus.ACTIVE, true);
		identityProvider.platformAdmins.add(id);
		return id;
	}
}
