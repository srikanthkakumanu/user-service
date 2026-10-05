package com.users.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.users.domain.exception.DuplicateUserException;
import com.users.domain.model.AccountStatus;
import com.users.domain.model.Credential;
import com.users.domain.model.Email;
import com.users.domain.model.NewUser;
import com.users.domain.model.PageResult;
import com.users.domain.model.Password;
import com.users.domain.model.RequiredAction;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.model.UserSearch;
import com.users.domain.port.IdentityProviderPort;

/** In-memory identity provider for use-case tests; records side effects and can be told to fail. */
class FakeIdentityProvider implements IdentityProviderPort {

	final Map<UserId, User> users = new LinkedHashMap<>();
	final Map<UserId, NewUser> created = new LinkedHashMap<>();
	final Map<UserId, String> passwords = new LinkedHashMap<>();
	final Map<UserId, Boolean> temporaryPasswords = new LinkedHashMap<>();
	final List<UserId> verificationEmails = new ArrayList<>();
	final List<UserId> resetEmails = new ArrayList<>();
	final List<UserId> signedOut = new ArrayList<>();
	final List<UserId> deleted = new ArrayList<>();
	final List<String> removedCredentials = new ArrayList<>();
	final Set<UserId> platformAdmins = new HashSet<>();
	RuntimeException failOnVerificationEmail;
	RuntimeException failOnDelete;

	UserId add(String username, String email, AccountStatus status, boolean emailVerified) {
		var id = UserId.of(UUID.randomUUID().toString());
		users.put(id, User.rehydrate(id, com.users.domain.model.Username.of(username), Email.of(email),
				com.users.domain.model.PersonName.of("Test", "User"), emailVerified, status, Set.of(),
				Instant.parse("2026-01-01T00:00:00Z")));
		return id;
	}

	@Override
	public UserId create(NewUser newUser) {
		boolean taken = users.values().stream().anyMatch(
				user -> user.username().equals(newUser.username()) || user.email().equals(newUser.email()));
		if (taken) {
			throw new DuplicateUserException();
		}
		var id = UserId.of(UUID.randomUUID().toString());
		users.put(id, User.rehydrate(id, newUser.username(), newUser.email(), newUser.name(),
				newUser.emailVerified(), AccountStatus.ACTIVE, newUser.requiredActions(), Instant.now()));
		created.put(id, newUser);
		return id;
	}

	@Override
	public Optional<User> findById(UserId id) {
		return Optional.ofNullable(users.get(id));
	}

	@Override
	public Optional<User> findByEmail(Email email) {
		return users.values().stream().filter(user -> user.email().equals(email)).findFirst();
	}

	@Override
	public PageResult<User> search(UserSearch search) {
		var all = List.copyOf(users.values());
		return new PageResult<>(all, all.size(), search.page(), search.size());
	}

	@Override
	public void save(User user) {
		users.put(user.id(), user);
	}

	@Override
	public void delete(UserId id) {
		if (failOnDelete != null) {
			throw failOnDelete;
		}
		users.remove(id);
		deleted.add(id);
	}

	@Override
	public void setRequiredActions(UserId id, Set<RequiredAction> actions) {
		users.get(id).require(actions);
	}

	@Override
	public void sendVerificationEmail(UserId id) {
		if (failOnVerificationEmail != null) {
			throw failOnVerificationEmail;
		}
		verificationEmails.add(id);
	}

	@Override
	public void sendPasswordResetEmail(UserId id) {
		resetEmails.add(id);
	}

	@Override
	public void setPassword(UserId id, Password password, boolean temporary) {
		passwords.put(id, password.value());
		temporaryPasswords.put(id, temporary);
	}

	@Override
	public List<Credential> listCredentials(UserId id) {
		return List.of(new Credential("cred-1", "password", null, Instant.parse("2026-01-01T00:00:00Z")));
	}

	@Override
	public void removeCredential(UserId id, String credentialId) {
		removedCredentials.add(credentialId);
	}

	@Override
	public void signOutEverywhere(UserId id) {
		signedOut.add(id);
	}

	@Override
	public boolean isPlatformAdmin(UserId id) {
		return platformAdmins.contains(id);
	}

	@Override
	public boolean isLastPlatformAdmin(UserId id) {
		return platformAdmins.contains(id)
				&& platformAdmins.stream().filter(admin -> users.get(admin).canSignIn()).count() == 1;
	}
}
