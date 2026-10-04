package user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import user.dto.*;
import user.service.UserService;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    private UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Retrieve All Users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found zero or more Users",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))}),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content)})
    public ResponseEntity<?> getAllUsers() {

        log.debug("Fetch all Users");

        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    @PostMapping
    @Operation(summary = "Create User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully Created new User",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to Create new User",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "User already Exists with Given Id",
                    content = @Content) })
    public ResponseEntity<?> createUser(
            @Parameter(description = "New User Body Content to be created")
            @Valid @RequestBody NewUserDTO newUserDTO) {

        log.debug("Create user: [user: {}]", newUserDTO.toString());

        UserDTO dto = userService.save(newUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the User matching this Id",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "User Not Found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Id type mismatch and not valid",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> getUserById(
            @Parameter(description = "id of User to be found")
            @PathVariable UUID id) {

        log.debug("Fetch User By Id: [Id: {}]", id);

        UserDTO dto = userService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Retrieve User by signup/sign-in Email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the User matching this signup/sign-in Email",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "User Not Found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> getUserBySignupEmail(
            @Parameter(description = "signup/sign-in email of User to be found aka loginId")
            @Email(message = "The email address is invalid.", flags = {Pattern.Flag.CASE_INSENSITIVE})
            @PathVariable String email) {

        log.debug("Fetch User By loginId(signup/sign-in Email): [email: {}]", email);

        UserDTO dto = userService.getUserByLoginId(email);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Updated User Password",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to Update User Password",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User Id does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> updateUser(
            @Parameter(description = "User Body Content to be updated")
            @Valid @RequestBody NewUserDTO dto, @PathVariable UUID id) {

        UserDTO updated = userService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Deleted User",
                    content = { @Content() }),
            @ApiResponse(responseCode = "400", description = "Failed to Delete User",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User Id does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "User Id to be deleted")
            @PathVariable UUID id) {

        log.debug("Delete user: [Id: {}]", id);

        UserDTO dto = userService.delete(id);

       return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PostMapping("/{id}/lock")
    @Operation(summary = "Lock User")
    public ResponseEntity<?> lockUser(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.lock(id));
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock User")
    public ResponseEntity<?> unlockUser(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.unlock(id));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Reset User Password")
    public ResponseEntity<?> resetPassword(
            @PathVariable UUID id,
            @Valid @RequestBody PasswordResetRequest request) {
        userService.resetPassword(id, request.temporaryPassword(), request.temporary());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "Assign User Role")
    public ResponseEntity<?> assignRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleAssignmentRequest request) {
        userService.assignRole(id, request.role());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/roles")
    @Operation(summary = "Remove User Role")
    public ResponseEntity<?> removeRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleAssignmentRequest request) {
        userService.removeRole(id, request.role());
        return ResponseEntity.noContent().build();
    }

//    @ExceptionHandler(ConstraintViolationException.class)
//    private ResponseEntity<?> constraintViolationException(ConstraintViolationException ex, WebRequest request) {
//        List<String> errors = new ArrayList<>();
//
//        ex.getConstraintViolations().forEach(cv -> errors.add(cv.getMessage()));
//
//        Map<String, List<String>> result = new HashMap<>();
//
//        result.put("errors", errors);
//
//        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
//    }
}
