package com.users.interfaces.rest;

import java.util.List;

import com.users.application.ListCredentials;
import com.users.application.RemoveCredential;
import com.users.application.ResetPassword;
import com.users.domain.model.UserId;
import com.users.interfaces.rest.dto.Requests;
import com.users.interfaces.rest.dto.Responses.CredentialResponse;
import com.users.interfaces.security.CurrentActor;
import com.users.interfaces.security.Permissions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Administration of another user's credentials. Changing your own password is auth-service's job. */
@RestController
@RequestMapping("/api/v1/users/{id}/credentials")
@Tag(name = "Credentials (admin)")
class CredentialController {

	private final ResetPassword resetPassword;
	private final ListCredentials listCredentials;
	private final RemoveCredential removeCredential;

	CredentialController(ResetPassword resetPassword, ListCredentials listCredentials,
			RemoveCredential removeCredential) {
		this.resetPassword = resetPassword;
		this.listCredentials = listCredentials;
		this.removeCredential = removeCredential;
	}

	@GetMapping
	@PreAuthorize(Permissions.READ)
	@Operation(summary = "List the kinds of credential a user holds; secrets are never returned")
	List<CredentialResponse> list(@PathVariable String id) {
		return listCredentials.handle(UserId.of(id)).stream().map(CredentialResponse::from).toList();
	}

	@PutMapping("/password")
	@PreAuthorize(Permissions.WRITE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Set a user's password; a temporary one must be changed at next sign-in")
	void setPassword(@PathVariable String id, @Valid @RequestBody Requests.SetPassword request,
			@AuthenticationPrincipal Jwt jwt) {
		resetPassword.handle(CurrentActor.from(jwt), UserId.of(id), request.password(), request.temporary());
	}

	@DeleteMapping("/{credentialId}")
	@PreAuthorize(Permissions.WRITE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void remove(@PathVariable String id, @PathVariable String credentialId, @AuthenticationPrincipal Jwt jwt) {
		removeCredential.handle(CurrentActor.from(jwt), UserId.of(id), credentialId);
	}
}
