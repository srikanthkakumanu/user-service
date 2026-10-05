package com.users.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.users.domain.model.Credential;
import com.users.domain.model.Email;
import com.users.domain.model.NewUser;
import com.users.domain.model.PageResult;
import com.users.domain.model.Password;
import com.users.domain.model.RequiredAction;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.model.UserSearch;

/**
 * The identity provider as this context sees it: the source of truth for identities, account
 * state and credentials. Replacing the provider means writing another implementation of this
 * port and nothing else.
 */
public interface IdentityProviderPort {

	/** @throws com.users.domain.exception.DuplicateUserException if the username or email is taken */
	UserId create(NewUser newUser);

	Optional<User> findById(UserId id);

	Optional<User> findByEmail(Email email);

	PageResult<User> search(UserSearch search);

	/** Stores email, name, verification state and account status of the given user. */
	void save(User user);

	void delete(UserId id);

	void setRequiredActions(UserId id, Set<RequiredAction> actions);

	void sendVerificationEmail(UserId id);

	void sendPasswordResetEmail(UserId id);

	/** @throws com.users.domain.exception.PasswordPolicyException if the realm policy rejects it */
	void setPassword(UserId id, Password password, boolean temporary);

	List<Credential> listCredentials(UserId id);

	/** @throws com.users.domain.exception.CredentialNotFoundException if the user has no such credential */
	void removeCredential(UserId id, String credentialId);

	/** Ends every session of the user, so refresh tokens stop working immediately. */
	void signOutEverywhere(UserId id);

	boolean isPlatformAdmin(UserId id);

	/** True when the user is the only enabled platform administrator. */
	boolean isLastPlatformAdmin(UserId id);
}
