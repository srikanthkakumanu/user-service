package com.users.interfaces.rest;

import java.net.URI;

import com.users.application.RegisterUser;
import com.users.application.RequestPasswordReset;
import com.users.domain.model.UserId;
import com.users.interfaces.rest.dto.CommandMapper;
import com.users.interfaces.rest.dto.Requests;
import com.users.interfaces.rest.dto.Responses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** The two things anyone may do without signing in. */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Registration")
class RegistrationController {

	private final RegisterUser registerUser;
	private final RequestPasswordReset requestPasswordReset;
	private final CommandMapper mapper;

	RegistrationController(RegisterUser registerUser, RequestPasswordReset requestPasswordReset, CommandMapper mapper) {
		this.registerUser = registerUser;
		this.requestPasswordReset = requestPasswordReset;
		this.mapper = mapper;
	}

	@PostMapping("/register")
	@Operation(summary = "Register yourself; a verification email is sent")
	ResponseEntity<Responses.Created> register(@Valid @RequestBody Requests.Register request) {
		UserId id = registerUser.handle(mapper.toCommand(request));
		return ResponseEntity.created(URI.create("/api/v1/users/" + id.value())).body(new Responses.Created(id.value()));
	}

	@PostMapping("/password-reset-requests")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Operation(summary = "Ask for a password-reset email; the answer is the same for unknown addresses")
	void requestPasswordReset(@Valid @RequestBody Requests.PasswordResetRequest request) {
		requestPasswordReset.handle(request.email());
	}
}
