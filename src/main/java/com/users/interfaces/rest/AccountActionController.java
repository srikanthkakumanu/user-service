package com.users.interfaces.rest;

import com.users.application.SendPasswordResetEmail;
import com.users.application.SendVerificationEmail;
import com.users.application.SetRequiredActions;
import com.users.domain.model.UserId;
import com.users.interfaces.rest.dto.Requests;
import com.users.interfaces.rest.dto.Responses.UserResponse;
import com.users.interfaces.security.CurrentActor;
import com.users.interfaces.security.Permissions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{id}/actions")
@PreAuthorize(Permissions.WRITE)
@Tag(name = "Account actions")
class AccountActionController {

	private final SendVerificationEmail sendVerificationEmail;
	private final SendPasswordResetEmail sendPasswordResetEmail;
	private final SetRequiredActions setRequiredActions;

	AccountActionController(SendVerificationEmail sendVerificationEmail,
			SendPasswordResetEmail sendPasswordResetEmail, SetRequiredActions setRequiredActions) {
		this.sendVerificationEmail = sendVerificationEmail;
		this.sendPasswordResetEmail = sendPasswordResetEmail;
		this.setRequiredActions = setRequiredActions;
	}

	@PostMapping("/send-verify-email")
	@ResponseStatus(HttpStatus.ACCEPTED)
	void sendVerifyEmail(@PathVariable String id) {
		sendVerificationEmail.handle(UserId.of(id));
	}

	@PostMapping("/send-reset-password-email")
	@ResponseStatus(HttpStatus.ACCEPTED)
	void sendResetPasswordEmail(@PathVariable String id) {
		sendPasswordResetEmail.handle(UserId.of(id));
	}

	@PutMapping("/required")
	@Operation(summary = "Replace what the user must do at next sign-in")
	UserResponse setRequiredActions(@PathVariable String id, @Valid @RequestBody Requests.SetRequiredActions request,
			@AuthenticationPrincipal Jwt jwt) {
		return UserResponse.from(setRequiredActions.handle(CurrentActor.from(jwt), UserId.of(id), request.actions()));
	}
}
