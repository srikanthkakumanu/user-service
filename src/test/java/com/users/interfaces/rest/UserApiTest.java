package com.users.interfaces.rest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.platform.security.autoconfigure.PlatformSecurityAutoConfiguration;
import com.platform.security.jwt.PlatformAuthoritiesConverter;
import com.users.application.CreateUser;
import com.users.application.DeleteUser;
import com.users.application.DisableUser;
import com.users.application.EnableUser;
import com.users.application.GetProfile;
import com.users.application.GetUser;
import com.users.application.ListCredentials;
import com.users.application.LockUser;
import com.users.application.RegisterUser;
import com.users.application.RemoveCredential;
import com.users.application.RequestPasswordReset;
import com.users.application.ResetPassword;
import com.users.application.SearchUsers;
import com.users.application.SendPasswordResetEmail;
import com.users.application.SendVerificationEmail;
import com.users.application.SetRequiredActions;
import com.users.application.UnlockUser;
import com.users.application.UpdateProfile;
import com.users.application.UpdateUser;
import com.users.domain.exception.DuplicateUserException;
import com.users.domain.exception.IdentityProviderUnavailableException;
import com.users.domain.exception.InvalidStateException;
import com.users.domain.exception.OperationNotPermittedException;
import com.users.domain.exception.PasswordPolicyException;
import com.users.domain.exception.UserNotFoundException;
import com.users.domain.model.AccountStatus;
import com.users.domain.model.Actor;
import com.users.domain.model.Credential;
import com.users.domain.model.Email;
import com.users.domain.model.PageResult;
import com.users.domain.model.PersonName;
import com.users.domain.model.RequiredAction;
import com.users.domain.model.User;
import com.users.domain.model.UserId;
import com.users.domain.model.UserProfile;
import com.users.domain.model.UserSearch;
import com.users.domain.model.Username;
import com.users.interfaces.rest.dto.CommandMapperImpl;
import com.users.interfaces.rest.error.ProblemResponses;
import com.users.interfaces.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Validation, error mapping and authorization rules of the user API, with use cases mocked. */
@WebMvcTest(properties = {
		"platform.security.jwt.issuer-uri=http://localhost:8080/realms/platform",
		"platform.security.jwt.jwk-set-uri=http://localhost:1/certs",
		"platform.security.jwt.audience=user-service" })
@ImportAutoConfiguration(PlatformSecurityAutoConfiguration.class)
@Import({ SecurityConfiguration.class, ProblemResponses.class, CommandMapperImpl.class })
class UserApiTest {

	private static final String ALICE = "7c1f0e9a-3b5d-4f6a-8b7c-9d0e1f2a3b4c";
	private static final String BOB = "11111111-2222-3333-4444-555555555555";
	private static final String VALID_REGISTRATION = """
			{"username": "alice", "email": "alice@example.com", "firstName": "Alice", "lastName": "Doe",
			 "password": "S3cret!Passw0rd"}""";

	@Autowired
	private MockMvc mvc;

	@MockitoBean private RegisterUser registerUser;
	@MockitoBean private RequestPasswordReset requestPasswordReset;
	@MockitoBean private CreateUser createUser;
	@MockitoBean private GetUser getUser;
	@MockitoBean private SearchUsers searchUsers;
	@MockitoBean private UpdateUser updateUser;
	@MockitoBean private EnableUser enableUser;
	@MockitoBean private DisableUser disableUser;
	@MockitoBean private LockUser lockUser;
	@MockitoBean private UnlockUser unlockUser;
	@MockitoBean private DeleteUser deleteUser;
	@MockitoBean private GetProfile getProfile;
	@MockitoBean private UpdateProfile updateProfile;
	@MockitoBean private SendVerificationEmail sendVerificationEmail;
	@MockitoBean private SendPasswordResetEmail sendPasswordResetEmail;
	@MockitoBean private SetRequiredActions setRequiredActions;
	@MockitoBean private ResetPassword resetPassword;
	@MockitoBean private ListCredentials listCredentials;
	@MockitoBean private RemoveCredential removeCredential;

	// --- public endpoints

	@Test
	void anyoneCanRegisterAndGetsTheNewUsersLocation() throws Exception {
		given(registerUser.handle(any())).willReturn(UserId.of(ALICE));

		mvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTRATION))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/users/" + ALICE))
				.andExpect(jsonPath("$.id").value(ALICE));

		var command = ArgumentCaptor.forClass(RegisterUser.Command.class);
		verify(registerUser).handle(command.capture());
		assertThat(command.getValue())
				.isEqualTo(new RegisterUser.Command("alice", "alice@example.com", "Alice", "Doe", "S3cret!Passw0rd"));
	}

	@Test
	void registrationValidatesTheBodyAndListsEveryInvalidField() throws Exception {
		mvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username": "al", "email": "not-an-email", "firstName": "", "lastName": "Doe"}"""))
				.andExpect(status().isBadRequest())
				.andExpect(problem("invalid-value"))
				.andExpect(jsonPath("$.errors[*].field")
						.value(org.hamcrest.Matchers.containsInAnyOrder("username", "email", "firstName", "password")));

		verifyNoInteractions(registerUser);
	}

	@Test
	void malformedJsonIsABadRequest() throws Exception {
		mvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON).content("{not json"))
				.andExpect(status().isBadRequest()).andExpect(problem("invalid-value"));
	}

	@Test
	void duplicateRegistrationIsAConflict() throws Exception {
		given(registerUser.handle(any())).willThrow(new DuplicateUserException());

		mvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTRATION))
				.andExpect(status().isConflict()).andExpect(problem("duplicate-user"));
	}

	@Test
	void aPasswordAgainstPolicyIsUnprocessable() throws Exception {
		given(registerUser.handle(any())).willThrow(new PasswordPolicyException());

		mvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTRATION))
				.andExpect(status().isUnprocessableContent()).andExpect(problem("password-policy"));
	}

	@Test
	void anyoneCanRequestAPasswordReset() throws Exception {
		mvc.perform(post("/api/v1/users/password-reset-requests").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "alice@example.com"}"""))
				.andExpect(status().isAccepted());

		verify(requestPasswordReset).handle("alice@example.com");
	}

	// --- authentication

	@Test
	void everythingElseNeedsAToken() throws Exception {
		mvc.perform(get("/api/v1/users")).andExpect(status().isUnauthorized()).andExpect(problem("unauthorized"));
		mvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized());
		mvc.perform(delete("/api/v1/users/" + BOB)).andExpect(status().isUnauthorized());
	}

	@Test
	void aTokenThatCannotBeVerifiedIsRejected() throws Exception {
		mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer not.a.token"))
				.andExpect(status().isUnauthorized()).andExpect(problem("unauthorized"));
	}

	// --- authorization

	@Test
	void searchNeedsTheReadPermission() throws Exception {
		given(searchUsers.handle(any())).willReturn(new PageResult<>(List.of(user(ALICE, "alice")), 41, 2, 20));

		mvc.perform(get("/api/v1/users").with(token(BOB))).andExpect(status().isForbidden())
				.andExpect(problem("forbidden"));
		mvc.perform(get("/api/v1/users?q=ali&enabled=true&page=2&size=20").with(token(BOB, "users:read")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(41)).andExpect(jsonPath("$.page").value(2))
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.items[0].username").value("alice"))
				.andExpect(jsonPath("$.items[0].status").value("ACTIVE"));

		verify(searchUsers).handle(new UserSearch("ali", true, 2, 20));
	}

	@Test
	void searchRejectsUnsupportedSortingAndOversizedPages() throws Exception {
		mvc.perform(get("/api/v1/users?sort=email,desc").with(token(BOB, "users:read")))
				.andExpect(status().isBadRequest()).andExpect(problem("invalid-value"))
				.andExpect(jsonPath("$.errors[0].field").value("sort"));
		mvc.perform(get("/api/v1/users?size=500").with(token(BOB, "users:read")))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("size"));
		mvc.perform(get("/api/v1/users?page=abc").with(token(BOB, "users:read")))
				.andExpect(status().isBadRequest()).andExpect(problem("invalid-value"));
	}

	@Test
	void aUserMayReadThemselvesButNotOthers() throws Exception {
		given(getUser.handle(UserId.of(ALICE))).willReturn(user(ALICE, "alice"));

		mvc.perform(get("/api/v1/users/" + ALICE).with(token(ALICE))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(ALICE)).andExpect(jsonPath("$.email").value("alice@example.com"));
		mvc.perform(get("/api/v1/users/" + ALICE).with(token(BOB))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/users/" + ALICE).with(token(BOB, "users:read"))).andExpect(status().isOk());
	}

	@Test
	void unknownUsersAndMalformedIdsAreReportedAsProblems() throws Exception {
		given(getUser.handle(UserId.of(BOB))).willThrow(new UserNotFoundException(UserId.of(BOB)));

		mvc.perform(get("/api/v1/users/" + BOB).with(token(ALICE, "users:read"))).andExpect(status().isNotFound())
				.andExpect(problem("user-not-found"));
		mvc.perform(get("/api/v1/users/not-a-uuid").with(token(ALICE, "users:read")))
				.andExpect(status().isBadRequest()).andExpect(problem("invalid-value"));
	}

	@Test
	void writingNeedsTheWritePermission() throws Exception {
		String body = """
				{"username": "bob", "email": "bob@example.com", "firstName": "Bob", "lastName": "Roe",
				 "temporaryPassword": "Temp!Passw0rd1", "emailVerified": true}""";
		given(createUser.handle(any())).willReturn(UserId.of(BOB));

		mvc.perform(post("/api/v1/users").with(token(ALICE, "users:read")).contentType(MediaType.APPLICATION_JSON)
				.content(body)).andExpect(status().isForbidden());
		mvc.perform(post("/api/v1/users/" + BOB + "/disable").with(token(ALICE, "users:read")))
				.andExpect(status().isForbidden());
		mvc.perform(delete("/api/v1/users/" + BOB).with(token(ALICE, "users:read"))).andExpect(status().isForbidden());
		mvc.perform(put("/api/v1/users/" + BOB + "/credentials/password").with(token(ALICE, "users:read"))
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"password": "Temp!Passw0rd1", "temporary": true}"""))
				.andExpect(status().isForbidden());
		verifyNoInteractions(createUser, disableUser, deleteUser, resetPassword);

		mvc.perform(post("/api/v1/users").with(token(ALICE, "users:write")).contentType(MediaType.APPLICATION_JSON)
				.content(body)).andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/users/" + BOB));
		verify(createUser).handle(new CreateUser.Command("bob", "bob@example.com", "Bob", "Roe", "Temp!Passw0rd1", true));
	}

	@Test
	void stateChangesPassTheCallerAsActor() throws Exception {
		given(disableUser.handle(any(), eq(UserId.of(BOB)))).willReturn(user(BOB, "bob"));

		mvc.perform(post("/api/v1/users/" + BOB + "/disable").with(token(ALICE, "users:write")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.username").value("bob"));

		var actor = ArgumentCaptor.forClass(Actor.class);
		verify(disableUser).handle(actor.capture(), eq(UserId.of(BOB)));
		assertThat(actor.getValue().id()).isEqualTo(UserId.of(ALICE));
		assertThat(actor.getValue().roles()).containsExactly("USER");
	}

	@Test
	void domainGuardsAndStateConflictsHaveTheirOwnProblemTypes() throws Exception {
		willThrow(new OperationNotPermittedException("The last platform administrator cannot be deleted"))
				.given(deleteUser).handle(any(), any());
		given(enableUser.handle(any(), any())).willThrow(new InvalidStateException("A locked user must be unlocked"));

		mvc.perform(delete("/api/v1/users/" + BOB).with(token(ALICE, "users:write")))
				.andExpect(status().isForbidden()).andExpect(problem("operation-not-permitted"))
				.andExpect(jsonPath("$.detail").value("The last platform administrator cannot be deleted"));
		mvc.perform(post("/api/v1/users/" + BOB + "/enable").with(token(ALICE, "users:write")))
				.andExpect(status().isConflict()).andExpect(problem("invalid-state"));
	}

	@Test
	void identityProviderFailuresNeverLeakDetails() throws Exception {
		given(getUser.handle(any())).willThrow(new IdentityProviderUnavailableException(
				"Identity provider failed to find user", new IllegalStateException("HTTP 500 from keycloak:8080 realm=platform")));

		mvc.perform(get("/api/v1/users/" + BOB).with(token(ALICE, "users:read")))
				.andExpect(status().isServiceUnavailable()).andExpect(problem("identity-provider-unavailable"))
				.andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsStringIgnoringCase("keycloak"))))
				.andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Exception"))));
	}

	@Test
	void unexpectedFailuresAreGenericServerErrors() throws Exception {
		given(getUser.handle(any())).willThrow(new IllegalStateException("connection pool exhausted at 10.0.0.5"));

		mvc.perform(get("/api/v1/users/" + BOB).with(token(ALICE, "users:read")))
				.andExpect(status().isInternalServerError()).andExpect(problem("internal-error"))
				.andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("10.0.0.5"))));
	}

	// --- profile

	@Test
	void meReadsAndUpdatesTheCallersOwnProfile() throws Exception {
		given(getProfile.handle(UserId.of(ALICE))).willReturn(profile(ALICE, "alice"));
		given(updateProfile.handle(any())).willReturn(profile(ALICE, "alice"));

		mvc.perform(get("/api/v1/users/me").with(token(ALICE))).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("alice")).andExpect(jsonPath("$.jobTitle").value("Engineer"));
		mvc.perform(put("/api/v1/users/me").with(token(ALICE)).contentType(MediaType.APPLICATION_JSON).content("""
				{"firstName": "Alicia", "lastName": "Roe", "jobTitle": "Lead", "timeZone": "Europe/London"}"""))
				.andExpect(status().isOk());

		verify(updateProfile).handle(new UpdateProfile.Command(UserId.of(ALICE), "Alicia", "Roe", null, "Lead", null,
				null, "Europe/London", null));
	}

	@Test
	void anotherUsersProfileNeedsPermission() throws Exception {
		String body = """
				{"firstName": "Alicia", "lastName": "Roe"}""";
		given(getProfile.handle(UserId.of(ALICE))).willReturn(profile(ALICE, "alice"));
		given(updateProfile.handle(any())).willReturn(profile(ALICE, "alice"));

		mvc.perform(get("/api/v1/users/" + ALICE + "/profile").with(token(BOB))).andExpect(status().isForbidden());
		mvc.perform(put("/api/v1/users/" + ALICE + "/profile").with(token(BOB, "users:read"))
				.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/users/" + ALICE + "/profile").with(token(BOB, "users:read"))).andExpect(status().isOk());
		mvc.perform(put("/api/v1/users/" + ALICE + "/profile").with(token(BOB, "users:write"))
				.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
		mvc.perform(put("/api/v1/users/" + ALICE + "/profile").with(token(ALICE))
				.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
	}

	// --- account actions and credentials

	@Test
	void accountActionsNeedTheWritePermission() throws Exception {
		given(setRequiredActions.handle(any(), any(), any())).willReturn(user(BOB, "bob"));

		mvc.perform(post("/api/v1/users/" + BOB + "/actions/send-verify-email").with(token(ALICE, "users:read")))
				.andExpect(status().isForbidden());
		mvc.perform(post("/api/v1/users/" + BOB + "/actions/send-verify-email").with(token(ALICE, "users:write")))
				.andExpect(status().isAccepted());
		mvc.perform(post("/api/v1/users/" + BOB + "/actions/send-reset-password-email").with(token(ALICE, "users:write")))
				.andExpect(status().isAccepted());
		mvc.perform(put("/api/v1/users/" + BOB + "/actions/required").with(token(ALICE, "users:write"))
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"actions": ["UPDATE_PASSWORD", "VERIFY_EMAIL"]}"""))
				.andExpect(status().isOk());

		verify(sendVerificationEmail).handle(UserId.of(BOB));
		verify(sendPasswordResetEmail).handle(UserId.of(BOB));
		verify(setRequiredActions).handle(any(), eq(UserId.of(BOB)),
				eq(Set.of(RequiredAction.UPDATE_PASSWORD, RequiredAction.VERIFY_EMAIL)));
	}

	@Test
	void unknownRequiredActionsAreRejected() throws Exception {
		mvc.perform(put("/api/v1/users/" + BOB + "/actions/required").with(token(ALICE, "users:write"))
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"actions": ["CONFIGURE_TOTP"]}"""))
				.andExpect(status().isBadRequest()).andExpect(problem("invalid-value"));
	}

	@Test
	void credentialsCanBeListedSetAndRemoved() throws Exception {
		given(listCredentials.handle(UserId.of(BOB)))
				.willReturn(List.of(new Credential("cred-1", "password", null, Instant.parse("2026-01-01T00:00:00Z"))));

		mvc.perform(get("/api/v1/users/" + BOB + "/credentials").with(token(ALICE, "users:read")))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].type").value("password"))
				.andExpect(jsonPath("$[0].id").value("cred-1"));
		mvc.perform(put("/api/v1/users/" + BOB + "/credentials/password").with(token(ALICE, "users:write"))
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"password": "Temp!Passw0rd1", "temporary": true}"""))
				.andExpect(status().isNoContent());
		mvc.perform(delete("/api/v1/users/" + BOB + "/credentials/cred-1").with(token(ALICE, "users:write")))
				.andExpect(status().isNoContent());

		verify(resetPassword).handle(any(), eq(UserId.of(BOB)), eq("Temp!Passw0rd1"), eq(true));
		verify(removeCredential).handle(any(), eq(UserId.of(BOB)), eq("cred-1"));
	}

	// --- helpers

	private static ResultMatcher problem(String code) {
		return result -> {
			content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON).match(result);
			jsonPath("$.type").value(ProblemResponses.TYPE_BASE + code).match(result);
			jsonPath("$.code").value(code).match(result);
			jsonPath("$.status").value(result.getResponse().getStatus()).match(result);
		};
	}

	/** A token for the given subject holding role USER and the given permissions, mapped by the real converter. */
	private static JwtRequestPostProcessor token(String subject, String... permissions) {
		return jwt().jwt(token -> token.subject(subject)
				.claim("realm_access", Map.of("roles", List.of("USER")))
				.claim("permissions", List.of(permissions)))
				.authorities(new PlatformAuthoritiesConverter());
	}

	private static User user(String id, String username) {
		return User.rehydrate(UserId.of(id), Username.of(username), Email.of(username + "@example.com"),
				PersonName.of("Test", "User"), true, AccountStatus.ACTIVE, Set.of(), Instant.parse("2026-01-01T00:00:00Z"));
	}

	private static GetProfile.Result profile(String id, String username) {
		var now = Instant.parse("2026-01-01T00:00:00Z");
		return new GetProfile.Result(user(id, username), UserProfile.rehydrate(UserId.of(id), null, "Engineer", null,
				null, null, null, now, now));
	}
}
