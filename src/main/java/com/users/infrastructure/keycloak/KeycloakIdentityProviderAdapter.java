package com.users.infrastructure.keycloak;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.users.domain.exception.CredentialNotFoundException;
import com.users.domain.exception.DuplicateUserException;
import com.users.domain.exception.IdentityProviderUnavailableException;
import com.users.domain.exception.PasswordPolicyException;
import com.users.domain.exception.UserNotFoundException;
import com.users.domain.model.AccountStatus;
import com.users.domain.model.Credential;
import com.users.domain.model.Email;
import com.users.domain.model.NewUser;
import com.users.domain.model.PageResult;
import com.users.domain.model.Password;
import com.users.domain.model.PersonName;
import com.users.domain.model.RequiredAction;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.model.UserSearch;
import com.users.domain.model.Username;
import com.users.domain.port.IdentityProviderPort;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

/**
 * Keycloak behind {@link IdentityProviderPort}. Nothing outside this package knows Keycloak
 * exists; its errors are translated to domain exceptions and never leak to callers.
 */
@Component
class KeycloakIdentityProviderAdapter implements IdentityProviderPort {

	/** Marks an account an administrator locked, as opposed to one that was merely disabled. */
	static final String LOCKED_ATTRIBUTE = "platform.locked";

	private static final Map<RequiredAction, String> ACTION_NAMES = Map.of(
			RequiredAction.VERIFY_EMAIL, "VERIFY_EMAIL",
			RequiredAction.UPDATE_PASSWORD, "UPDATE_PASSWORD",
			RequiredAction.UPDATE_PROFILE, "UPDATE_PROFILE");

	/**
	 * Obtains a new service-account token. The admin client caches its token; when the key that
	 * signed it is retired (signing-key rotation) Keycloak answers 401 until a new one is fetched.
	 */
	private static volatile Runnable renewToken = () -> {
	};

	static void onUnauthorized(Runnable renewal) {
		renewToken = renewal;
	}

	private final RealmResource realm;
	private final UserQueryResource userQueries;
	private final KeycloakProperties properties;

	KeycloakIdentityProviderAdapter(RealmResource realm, UserQueryResource userQueries, KeycloakProperties properties) {
		this.realm = realm;
		this.userQueries = userQueries;
		this.properties = properties;
	}

	@Override
	public UserId create(NewUser newUser) {
		var representation = new UserRepresentation();
		representation.setUsername(newUser.username().value());
		representation.setEmail(newUser.email().value());
		representation.setFirstName(newUser.name().firstName());
		representation.setLastName(newUser.name().lastName());
		representation.setEnabled(true);
		representation.setEmailVerified(newUser.emailVerified());
		representation.setRequiredActions(actionNames(newUser.requiredActions()));
		if (newUser.password() != null) {
			representation.setCredentials(List.of(passwordCredential(newUser.password(), newUser.temporaryPassword())));
		}
		return call("create user", () -> {
			try (Response response = realm.users().create(representation)) {
				return switch (response.getStatus()) {
					case 201 -> UserId.of(CreatedResponseUtil.getCreatedId(response));
					case 409 -> throw new DuplicateUserException();
					case 400 -> throw new PasswordPolicyException();
					default -> throw new WebApplicationException(response.getStatus());
				};
			}
		});
	}

	@Override
	public Optional<User> findById(UserId id) {
		return call("find user", () -> {
			try {
				return Optional.of(toUser(realm.users().get(id.value()).toRepresentation()));
			}
			catch (NotFoundException ex) {
				return Optional.empty();
			}
		});
	}

	@Override
	public Optional<User> findByEmail(Email email) {
		return call("find user by email",
				() -> realm.users().searchByEmail(email.value(), true).stream().findFirst().map(this::toUser));
	}

	@Override
	public PageResult<User> search(UserSearch search) {
		return call("search users", () -> {
			// Keycloak matches the term against username, email, first and last name, ordered by username.
			List<User> users = userQueries
					.search(properties.realm(), search.query(), search.enabled(), search.offset(), search.size(), false)
					.stream().map(this::toUser).toList();
			long total = userQueries.count(properties.realm(), search.query(), search.enabled());
			return new PageResult<>(users, total, search.page(), search.size());
		});
	}

	@Override
	public void save(User user) {
		call("update user", () -> {
			UserResource resource = existing(user.id());
			UserRepresentation representation = resource.toRepresentation();
			representation.setEmail(user.email().value());
			representation.setFirstName(user.name().firstName());
			representation.setLastName(user.name().lastName());
			representation.setEmailVerified(user.emailVerified());
			representation.setEnabled(user.status() == AccountStatus.ACTIVE);
			Map<String, List<String>> attributes = representation.getAttributes() == null ? new HashMap<>()
					: new HashMap<>(representation.getAttributes());
			if (user.status() == AccountStatus.LOCKED) {
				attributes.put(LOCKED_ATTRIBUTE, List.of("true"));
			}
			else {
				attributes.remove(LOCKED_ATTRIBUTE);
			}
			representation.setAttributes(attributes);
			try {
				resource.update(representation);
			}
			catch (WebApplicationException ex) {
				if (ex.getResponse().getStatus() == 409) {
					throw new DuplicateUserException();
				}
				throw ex;
			}
			if (user.status() == AccountStatus.ACTIVE) {
				// Also lift a temporary lockout caused by failed sign-in attempts.
				realm.attackDetection().clearBruteForceForUser(user.id().value());
			}
			return null;
		});
	}

	@Override
	public void delete(UserId id) {
		call("delete user", () -> {
			try (Response response = realm.users().delete(id.value())) {
				if (response.getStatus() >= 400 && response.getStatus() != 404) {
					throw new WebApplicationException(response.getStatus());
				}
			}
			return null;
		});
	}

	@Override
	public void setRequiredActions(UserId id, Set<RequiredAction> actions) {
		call("set required actions", () -> {
			UserResource resource = existing(id);
			UserRepresentation representation = resource.toRepresentation();
			representation.setRequiredActions(actionNames(actions));
			resource.update(representation);
			return null;
		});
	}

	@Override
	public void sendVerificationEmail(UserId id) {
		call("send verification email", () -> {
			existing(id).sendVerifyEmail();
			return null;
		});
	}

	@Override
	public void sendPasswordResetEmail(UserId id) {
		call("send password reset email", () -> {
			existing(id).executeActionsEmail(List.of("UPDATE_PASSWORD"));
			return null;
		});
	}

	@Override
	public void setPassword(UserId id, Password password, boolean temporary) {
		call("set password", () -> {
			try {
				existing(id).resetPassword(passwordCredential(password, temporary));
			}
			catch (BadRequestException ex) {
				throw new PasswordPolicyException();
			}
			return null;
		});
	}

	@Override
	public List<Credential> listCredentials(UserId id) {
		return call("list credentials", () -> existing(id).credentials().stream()
				.map(credential -> new Credential(credential.getId(), credential.getType(), credential.getUserLabel(),
						credential.getCreatedDate() == null ? null : Instant.ofEpochMilli(credential.getCreatedDate())))
				.toList());
	}

	@Override
	public void removeCredential(UserId id, String credentialId) {
		call("remove credential", () -> {
			UserResource resource = existing(id);
			boolean present = resource.credentials().stream().anyMatch(c -> c.getId().equals(credentialId));
			if (!present) {
				throw new CredentialNotFoundException(credentialId);
			}
			resource.removeCredential(credentialId);
			return null;
		});
	}

	@Override
	public void signOutEverywhere(UserId id) {
		call("sign out user", () -> {
			existing(id).logout();
			return null;
		});
	}

	@Override
	public boolean isPlatformAdmin(UserId id) {
		return call("read user roles", () -> existing(id).roles().realmLevel().listEffective().stream()
				.map(RoleRepresentation::getName).anyMatch(properties.platformAdminRole()::equals));
	}

	@Override
	public boolean isLastPlatformAdmin(UserId id) {
		return call("count platform administrators", () -> {
			List<UserRepresentation> administrators = realm.roles().get(properties.platformAdminRole())
					.getUserMembers(0, 100).stream().filter(member -> Boolean.TRUE.equals(member.isEnabled())).toList();
			return administrators.size() == 1 && administrators.getFirst().getId().equals(id.value());
		});
	}

	/** Resolves the user resource and proves the user exists, so later calls fail predictably. */
	private UserResource existing(UserId id) {
		UserResource resource = realm.users().get(id.value());
		try {
			resource.toRepresentation();
		}
		catch (NotFoundException ex) {
			throw new UserNotFoundException(id);
		}
		return resource;
	}

	private User toUser(UserRepresentation representation) {
		boolean enabled = Boolean.TRUE.equals(representation.isEnabled());
		boolean locked = representation.getAttributes() != null
				&& representation.getAttributes().getOrDefault(LOCKED_ATTRIBUTE, List.of()).contains("true");
		AccountStatus status = enabled ? AccountStatus.ACTIVE : locked ? AccountStatus.LOCKED : AccountStatus.DISABLED;
		return User.rehydrate(UserId.of(representation.getId()), Username.of(representation.getUsername()),
				Email.of(representation.getEmail()),
				PersonName.of(representation.getFirstName(), representation.getLastName()),
				Boolean.TRUE.equals(representation.isEmailVerified()), status,
				requiredActions(representation.getRequiredActions()),
				representation.getCreatedTimestamp() == null ? null
						: Instant.ofEpochMilli(representation.getCreatedTimestamp()));
	}

	private static Set<RequiredAction> requiredActions(List<String> names) {
		var actions = EnumSet.noneOf(RequiredAction.class);
		if (names != null) {
			ACTION_NAMES.forEach((action, name) -> {
				if (names.contains(name)) {
					actions.add(action);
				}
			});
		}
		return actions;
	}

	private static List<String> actionNames(Set<RequiredAction> actions) {
		return new ArrayList<>(actions.stream().map(ACTION_NAMES::get).sorted().toList());
	}

	private static CredentialRepresentation passwordCredential(Password password, boolean temporary) {
		var credential = new CredentialRepresentation();
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(password.value());
		credential.setTemporary(temporary);
		return credential;
	}

	/** Lets domain exceptions through and turns every other failure into "provider unavailable". */
	private static <T> T call(String operation, Supplier<T> action) {
		try {
			try {
				return action.get();
			}
			catch (jakarta.ws.rs.NotAuthorizedException ex) {
				renewToken.run();
				return action.get();
			}
		}
		catch (com.users.domain.exception.DomainException ex) {
			throw ex;
		}
		catch (WebApplicationException | ProcessingException ex) {
			throw new IdentityProviderUnavailableException("Identity provider failed to " + operation, ex);
		}
	}
}
