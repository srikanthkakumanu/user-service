package com.users.infrastructure.keycloak;

import java.util.Set;
import java.util.UUID;

import com.users.domain.exception.CredentialNotFoundException;
import com.users.domain.exception.DuplicateUserException;
import com.users.domain.exception.PasswordPolicyException;
import com.users.domain.exception.UserNotFoundException;
import com.users.domain.model.AccountStatus;
import com.users.domain.model.Email;
import com.users.domain.model.NewUser;
import com.users.domain.model.Password;
import com.users.domain.model.PersonName;
import com.users.domain.model.RequiredAction;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.model.UserSearch;
import com.users.domain.model.Username;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/** The Keycloak adapter against a real Keycloak running the platform realm file. */
class KeycloakIdentityProviderAdapterIT {

	private static final String PASSWORD = "S3cret!Passw0rd";

	private static KeycloakIdentityProviderAdapter adapter;

	@BeforeAll
	static void connect() {
		var properties = KeycloakTestEnvironment.userServiceProperties();
		var configuration = new KeycloakConfiguration();
		var keycloak = configuration.keycloakAdminClient(properties);
		adapter = new KeycloakIdentityProviderAdapter(configuration.platformRealm(keycloak, properties),
				configuration.userQueryResource(keycloak, properties), properties);
	}

	@Test
	void createsAUserThatCanBeFoundByIdAndEmail() {
		String name = unique();
		UserId id = adapter.create(selfRegistered(name));

		User found = adapter.findById(id).orElseThrow();

		assertThat(found.username().value()).isEqualTo(name);
		assertThat(found.email().value()).isEqualTo(name + "@example.com");
		assertThat(found.name().fullName()).isEqualTo("Test User");
		assertThat(found.status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(found.emailVerified()).isFalse();
		assertThat(found.requiredActions()).containsExactly(RequiredAction.VERIFY_EMAIL);
		assertThat(found.createdAt()).isNotNull();
		assertThat(adapter.findByEmail(Email.of(name + "@example.com"))).map(User::id).contains(id);
	}

	@Test
	void unknownUsersAreAbsent() {
		var unknown = UserId.of(UUID.randomUUID().toString());

		assertThat(adapter.findById(unknown)).isEmpty();
		assertThat(adapter.findByEmail(Email.of("nobody-" + unique() + "@example.com"))).isEmpty();
		assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> adapter.signOutEverywhere(unknown));
		assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> adapter.listCredentials(unknown));
	}

	@Test
	void duplicateUsernameAndDuplicateEmailAreConflicts() {
		String name = unique();
		adapter.create(selfRegistered(name));

		assertThatExceptionOfType(DuplicateUserException.class).isThrownBy(() -> adapter.create(
				NewUser.selfRegistered(Username.of(name), Email.of(unique() + "@example.com"),
						PersonName.of("Test", "User"), Password.of(PASSWORD))));
		assertThatExceptionOfType(DuplicateUserException.class).isThrownBy(() -> adapter.create(
				NewUser.selfRegistered(Username.of(unique()), Email.of(name + "@example.com"),
						PersonName.of("Test", "User"), Password.of(PASSWORD))));
	}

	@Test
	void aPasswordThatBreaksTheRealmPolicyIsRejectedOnCreateAndOnReset() {
		String name = unique();

		assertThatExceptionOfType(PasswordPolicyException.class).isThrownBy(() -> adapter.create(
				NewUser.selfRegistered(Username.of(name), Email.of(name + "@example.com"),
						PersonName.of("Test", "User"), Password.of("short"))));
		assertThat(adapter.findByEmail(Email.of(name + "@example.com"))).isEmpty();

		UserId id = adapter.create(selfRegistered(unique()));
		assertThatExceptionOfType(PasswordPolicyException.class)
				.isThrownBy(() -> adapter.setPassword(id, Password.of("alllowercase"), false));
	}

	@Test
	void searchFiltersPagesAndCounts() {
		String prefix = "srch" + UUID.randomUUID().toString().substring(0, 6);
		UserId first = adapter.create(selfRegistered(prefix + "-a"));
		adapter.create(selfRegistered(prefix + "-b"));
		UserId third = adapter.create(selfRegistered(prefix + "-c"));
		User disabled = adapter.findById(third).orElseThrow();
		disabled.disable();
		adapter.save(disabled);

		var firstPage = adapter.search(new UserSearch(prefix, null, 0, 2));
		var secondPage = adapter.search(new UserSearch(prefix, null, 1, 2));
		var enabledOnly = adapter.search(new UserSearch(prefix, true, 0, 10));
		var disabledOnly = adapter.search(new UserSearch(prefix, false, 0, 10));

		assertThat(firstPage.total()).isEqualTo(3);
		assertThat(firstPage.items()).extracting(user -> user.username().value())
				.containsExactly(prefix + "-a", prefix + "-b");
		assertThat(firstPage.items().getFirst().id()).isEqualTo(first);
		assertThat(secondPage.items()).extracting(user -> user.username().value()).containsExactly(prefix + "-c");
		assertThat(enabledOnly.total()).isEqualTo(2);
		assertThat(disabledOnly.items()).extracting(User::id).containsExactly(third);
	}

	@Test
	void savesEmailNameAndVerificationState() {
		UserId id = adapter.create(selfRegistered(unique()));
		User user = adapter.findById(id).orElseThrow();
		String newEmail = unique() + "@example.com";

		user.changeEmail(Email.of(newEmail));
		user.rename(PersonName.of("Alicia", "Roe"));
		adapter.save(user);

		User saved = adapter.findById(id).orElseThrow();
		assertThat(saved.email().value()).isEqualTo(newEmail);
		assertThat(saved.name().fullName()).isEqualTo("Alicia Roe");
		assertThat(saved.emailVerified()).isFalse();
	}

	@Test
	void changingTheEmailToOneInUseIsAConflict() {
		String taken = unique();
		adapter.create(selfRegistered(taken));
		UserId id = adapter.create(selfRegistered(unique()));
		User user = adapter.findById(id).orElseThrow();

		user.changeEmail(Email.of(taken + "@example.com"));

		assertThatExceptionOfType(DuplicateUserException.class).isThrownBy(() -> adapter.save(user));
	}

	@Test
	void distinguishesLockedFromDisabledAndBlocksSignInForBoth() {
		String name = unique();
		UserId id = verifiedUser(name);
		assertThat(KeycloakTestEnvironment.accessToken(name, PASSWORD)).isNotBlank();

		User user = adapter.findById(id).orElseThrow();
		user.lock();
		adapter.save(user);
		assertThat(adapter.findById(id).orElseThrow().status()).isEqualTo(AccountStatus.LOCKED);
		assertSignInRejected(name);

		user.unlock();
		adapter.save(user);
		assertThat(adapter.findById(id).orElseThrow().status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(KeycloakTestEnvironment.accessToken(name, PASSWORD)).isNotBlank();

		user.disable();
		adapter.save(user);
		assertThat(adapter.findById(id).orElseThrow().status()).isEqualTo(AccountStatus.DISABLED);
		assertSignInRejected(name);
	}

	@Test
	void replacesRequiredActions() {
		UserId id = adapter.create(selfRegistered(unique()));

		adapter.setRequiredActions(id, Set.of(RequiredAction.UPDATE_PASSWORD, RequiredAction.UPDATE_PROFILE));

		assertThat(adapter.findById(id).orElseThrow().requiredActions())
				.containsExactlyInAnyOrder(RequiredAction.UPDATE_PASSWORD, RequiredAction.UPDATE_PROFILE);

		adapter.setRequiredActions(id, Set.of());
		assertThat(adapter.findById(id).orElseThrow().requiredActions()).isEmpty();
	}

	@Test
	void sendsVerificationAndPasswordResetEmails() {
		String name = unique();
		UserId id = adapter.create(selfRegistered(name));

		adapter.sendVerificationEmail(id);
		adapter.sendPasswordResetEmail(id);

		assertThat(KeycloakTestEnvironment.emailSubjectsFor(name + "@example.com")).hasSize(2)
				.anyMatch(subject -> subject.toLowerCase().contains("verify"));
	}

	@Test
	void setsPasswordsAndATemporaryOneMustBeChanged() {
		String name = unique();
		UserId id = verifiedUser(name);

		adapter.setPassword(id, Password.of("An0ther!Passw0rd"), false);
		assertThat(KeycloakTestEnvironment.accessToken(name, "An0ther!Passw0rd")).isNotBlank();
		assertSignInRejected(name);

		adapter.setPassword(id, Password.of("Temp0rary!Passw0rd"), true);
		assertThat(adapter.findById(id).orElseThrow().requiredActions()).contains(RequiredAction.UPDATE_PASSWORD);
		assertThatExceptionOfType(HttpClientErrorException.class)
				.isThrownBy(() -> KeycloakTestEnvironment.accessToken(name, "Temp0rary!Passw0rd"));
	}

	@Test
	void listsAndRemovesCredentials() {
		String name = unique();
		UserId id = verifiedUser(name);

		var credentials = adapter.listCredentials(id);
		assertThat(credentials).hasSize(1);
		assertThat(credentials.getFirst().type()).isEqualTo("password");
		assertThat(credentials.getFirst().createdAt()).isNotNull();

		adapter.removeCredential(id, credentials.getFirst().id());

		assertThat(adapter.listCredentials(id)).isEmpty();
		assertSignInRejected(name);
		assertThatExceptionOfType(CredentialNotFoundException.class)
				.isThrownBy(() -> adapter.removeCredential(id, credentials.getFirst().id()));
	}

	@Test
	void signingOutEverywhereEndsTheUsersSessions() {
		String name = unique();
		UserId id = verifiedUser(name);
		KeycloakTestEnvironment.accessToken(name, PASSWORD);
		var sessions = KeycloakTestEnvironment.masterAdmin().realm(KeycloakTestEnvironment.REALM).users().get(id.value());
		assertThat(sessions.getUserSessions()).isNotEmpty();

		adapter.signOutEverywhere(id);

		assertThat(sessions.getUserSessions()).isEmpty();
	}

	@Test
	void recognisesPlatformAdministratorsAndTheLastOne() {
		UserId admin = adapter.search(new UserSearch("platform-admin", null, 0, 5)).items().getFirst().id();
		UserId ordinary = adapter.create(selfRegistered(unique()));

		assertThat(adapter.isPlatformAdmin(admin)).isTrue();
		assertThat(adapter.isLastPlatformAdmin(admin)).isTrue();
		assertThat(adapter.isPlatformAdmin(ordinary)).isFalse();
		assertThat(adapter.isLastPlatformAdmin(ordinary)).isFalse();
	}

	@Test
	void deletesAUserAndToleratesDeletingAgain() {
		UserId id = adapter.create(selfRegistered(unique()));

		adapter.delete(id);
		adapter.delete(id);

		assertThat(adapter.findById(id)).isEmpty();
	}

	private static UserId verifiedUser(String name) {
		return adapter.create(new NewUser(Username.of(name), Email.of(name + "@example.com"),
				PersonName.of("Test", "User"), Password.of(PASSWORD), false, true, Set.of()));
	}

	private static NewUser selfRegistered(String name) {
		return NewUser.selfRegistered(Username.of(name), Email.of(name + "@example.com"),
				PersonName.of("Test", "User"), Password.of(PASSWORD));
	}

	private static void assertSignInRejected(String username) {
		assertThatExceptionOfType(HttpClientErrorException.class)
				.isThrownBy(() -> KeycloakTestEnvironment.accessToken(username, PASSWORD));
	}

	private static String unique() {
		return "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}
