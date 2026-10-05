package com.users.interfaces.rest;

import java.net.URI;

import com.users.application.CreateUser;
import com.users.application.DeleteUser;
import com.users.application.DisableUser;
import com.users.application.EnableUser;
import com.users.application.GetUser;
import com.users.application.LockUser;
import com.users.application.SearchUsers;
import com.users.application.UnlockUser;
import com.users.application.UpdateUser;
import com.users.domain.exception.InvalidValueException;
import com.users.domain.model.UserId;
import com.users.domain.model.UserSearch;
import com.users.interfaces.rest.dto.CommandMapper;
import com.users.interfaces.rest.dto.Requests;
import com.users.interfaces.rest.dto.Responses;
import com.users.interfaces.rest.dto.Responses.PageResponse;
import com.users.interfaces.rest.dto.Responses.UserResponse;
import com.users.interfaces.security.CurrentActor;
import com.users.interfaces.security.Permissions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
class UserController {

	private final CreateUser createUser;
	private final GetUser getUser;
	private final SearchUsers searchUsers;
	private final UpdateUser updateUser;
	private final EnableUser enableUser;
	private final DisableUser disableUser;
	private final LockUser lockUser;
	private final UnlockUser unlockUser;
	private final DeleteUser deleteUser;
	private final CommandMapper mapper;

	UserController(CreateUser createUser, GetUser getUser, SearchUsers searchUsers, UpdateUser updateUser,
			EnableUser enableUser, DisableUser disableUser, LockUser lockUser, UnlockUser unlockUser,
			DeleteUser deleteUser, CommandMapper mapper) {
		this.createUser = createUser;
		this.getUser = getUser;
		this.searchUsers = searchUsers;
		this.updateUser = updateUser;
		this.enableUser = enableUser;
		this.disableUser = disableUser;
		this.lockUser = lockUser;
		this.unlockUser = unlockUser;
		this.deleteUser = deleteUser;
		this.mapper = mapper;
	}

	@PostMapping
	@PreAuthorize(Permissions.WRITE)
	@Operation(summary = "Create a user; a supplied password is temporary")
	ResponseEntity<Responses.Created> create(@Valid @RequestBody Requests.CreateUser request) {
		UserId id = createUser.handle(mapper.toCommand(request));
		return ResponseEntity.created(URI.create("/api/v1/users/" + id.value())).body(new Responses.Created(id.value()));
	}

	@GetMapping
	@PreAuthorize(Permissions.READ)
	@Operation(summary = "Search users by username, email or name; ordered by username")
	PageResponse<UserResponse> search(@RequestParam(required = false) String q,
			@RequestParam(required = false) Boolean enabled, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "username,asc") String sort) {
		if (!sort.equals("username,asc") && !sort.equals("username")) {
			throw new InvalidValueException("sort", "Users can only be sorted by username,asc");
		}
		return PageResponse.from(searchUsers.handle(new UserSearch(q, enabled, page, size)).map(UserResponse::from));
	}

	@GetMapping("/{id}")
	@PreAuthorize(Permissions.READ_OR_SELF)
	UserResponse get(@PathVariable String id) {
		return UserResponse.from(getUser.handle(UserId.of(id)));
	}

	@PutMapping("/{id}")
	@PreAuthorize(Permissions.WRITE)
	@Operation(summary = "Change a user's email and name; a new email must be verified again")
	UserResponse update(@PathVariable String id, @Valid @RequestBody Requests.UpdateUser request,
			@AuthenticationPrincipal Jwt jwt) {
		return UserResponse.from(updateUser.handle(CurrentActor.from(jwt), mapper.toCommand(UserId.of(id), request)));
	}

	@PostMapping("/{id}/enable")
	@PreAuthorize(Permissions.WRITE)
	UserResponse enable(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
		return UserResponse.from(enableUser.handle(CurrentActor.from(jwt), UserId.of(id)));
	}

	@PostMapping("/{id}/disable")
	@PreAuthorize(Permissions.WRITE)
	@Operation(summary = "Disable a user and end their sessions")
	UserResponse disable(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
		return UserResponse.from(disableUser.handle(CurrentActor.from(jwt), UserId.of(id)));
	}

	@PostMapping("/{id}/lock")
	@PreAuthorize(Permissions.WRITE)
	@Operation(summary = "Lock a user until an administrator unlocks them, and end their sessions")
	UserResponse lock(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
		return UserResponse.from(lockUser.handle(CurrentActor.from(jwt), UserId.of(id)));
	}

	@PostMapping("/{id}/unlock")
	@PreAuthorize(Permissions.WRITE)
	UserResponse unlock(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
		return UserResponse.from(unlockUser.handle(CurrentActor.from(jwt), UserId.of(id)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(Permissions.WRITE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
		deleteUser.handle(CurrentActor.from(jwt), UserId.of(id));
	}
}
