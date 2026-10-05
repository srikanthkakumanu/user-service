package com.users.infrastructure.config;

import java.time.Clock;

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
import com.users.domain.port.DomainEventPublisher;
import com.users.domain.port.IdentityProviderPort;
import com.users.domain.port.UserProfileRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the use cases. They are plain classes with no framework annotations, so the application
 * layer depends on nothing but the domain.
 */
@Configuration(proxyBeanMethods = false)
class UseCaseConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	RegisterUser registerUser(IdentityProviderPort identityProvider, UserProfileRepository profiles, DomainEventPublisher events, Clock clock) {
		return new RegisterUser(identityProvider, profiles, events, clock);
	}

	@Bean
	CreateUser createUser(IdentityProviderPort identityProvider, UserProfileRepository profiles, DomainEventPublisher events, Clock clock) {
		return new CreateUser(identityProvider, profiles, events, clock);
	}

	@Bean
	GetUser getUser(IdentityProviderPort identityProvider) {
		return new GetUser(identityProvider);
	}

	@Bean
	SearchUsers searchUsers(IdentityProviderPort identityProvider) {
		return new SearchUsers(identityProvider);
	}

	@Bean
	UpdateUser updateUser(IdentityProviderPort identityProvider, DomainEventPublisher events, Clock clock) {
		return new UpdateUser(identityProvider, events, clock);
	}

	@Bean
	EnableUser enableUser(IdentityProviderPort identityProvider, DomainEventPublisher events, Clock clock) {
		return new EnableUser(identityProvider, events, clock);
	}

	@Bean
	DisableUser disableUser(IdentityProviderPort identityProvider, DomainEventPublisher events, Clock clock) {
		return new DisableUser(identityProvider, events, clock);
	}

	@Bean
	LockUser lockUser(IdentityProviderPort identityProvider, DomainEventPublisher events, Clock clock) {
		return new LockUser(identityProvider, events, clock);
	}

	@Bean
	UnlockUser unlockUser(IdentityProviderPort identityProvider, DomainEventPublisher events, Clock clock) {
		return new UnlockUser(identityProvider, events, clock);
	}

	@Bean
	DeleteUser deleteUser(IdentityProviderPort identityProvider, UserProfileRepository profiles, DomainEventPublisher events, Clock clock) {
		return new DeleteUser(identityProvider, profiles, events, clock);
	}

	@Bean
	GetProfile getProfile(IdentityProviderPort identityProvider, UserProfileRepository profiles, Clock clock) {
		return new GetProfile(identityProvider, profiles, clock);
	}

	@Bean
	UpdateProfile updateProfile(IdentityProviderPort identityProvider, UserProfileRepository profiles, DomainEventPublisher events, Clock clock) {
		return new UpdateProfile(identityProvider, profiles, events, clock);
	}

	@Bean
	SendVerificationEmail sendVerificationEmail(IdentityProviderPort identityProvider) {
		return new SendVerificationEmail(identityProvider);
	}

	@Bean
	SendPasswordResetEmail sendPasswordResetEmail(IdentityProviderPort identityProvider) {
		return new SendPasswordResetEmail(identityProvider);
	}

	@Bean
	RequestPasswordReset requestPasswordReset(IdentityProviderPort identityProvider) {
		return new RequestPasswordReset(identityProvider);
	}

	@Bean
	SetRequiredActions setRequiredActions(IdentityProviderPort identityProvider) {
		return new SetRequiredActions(identityProvider);
	}

	@Bean
	ResetPassword resetPassword(IdentityProviderPort identityProvider, DomainEventPublisher events, Clock clock) {
		return new ResetPassword(identityProvider, events, clock);
	}

	@Bean
	ListCredentials listCredentials(IdentityProviderPort identityProvider) {
		return new ListCredentials(identityProvider);
	}

	@Bean
	RemoveCredential removeCredential(IdentityProviderPort identityProvider) {
		return new RemoveCredential(identityProvider);
	}
}
