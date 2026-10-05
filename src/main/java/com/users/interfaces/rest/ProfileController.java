package com.users.interfaces.rest;

import com.users.application.GetProfile;
import com.users.application.UpdateProfile;
import com.users.domain.model.UserId;
import com.users.interfaces.rest.dto.CommandMapper;
import com.users.interfaces.rest.dto.Requests;
import com.users.interfaces.rest.dto.Responses.ProfileResponse;
import com.users.interfaces.security.Permissions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A user's own profile under {@code /me}; any profile for callers with the matching permission. */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Profile")
class ProfileController {

	private final GetProfile getProfile;
	private final UpdateProfile updateProfile;
	private final CommandMapper mapper;

	ProfileController(GetProfile getProfile, UpdateProfile updateProfile, CommandMapper mapper) {
		this.getProfile = getProfile;
		this.updateProfile = updateProfile;
		this.mapper = mapper;
	}

	@GetMapping("/me")
	@Operation(summary = "Your own identity and profile")
	ProfileResponse me(@AuthenticationPrincipal Jwt jwt) {
		return ProfileResponse.from(getProfile.handle(UserId.of(jwt.getSubject())));
	}

	@PutMapping("/me")
	@Operation(summary = "Update your own name and profile")
	ProfileResponse updateMe(@Valid @RequestBody Requests.UpdateProfile request, @AuthenticationPrincipal Jwt jwt) {
		return ProfileResponse.from(updateProfile.handle(mapper.toCommand(UserId.of(jwt.getSubject()), request)));
	}

	@GetMapping("/{id}/profile")
	@PreAuthorize(Permissions.READ_OR_SELF)
	ProfileResponse get(@PathVariable String id) {
		return ProfileResponse.from(getProfile.handle(UserId.of(id)));
	}

	@PutMapping("/{id}/profile")
	@PreAuthorize(Permissions.WRITE_OR_SELF)
	ProfileResponse update(@PathVariable String id, @Valid @RequestBody Requests.UpdateProfile request) {
		return ProfileResponse.from(updateProfile.handle(mapper.toCommand(UserId.of(id), request)));
	}
}
