package user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import user.common.enums.UserStatus;
import user.dto.NewUserDTO;
import user.dto.UpdatePasswordDTO;
import user.dto.UserDTO;
import user.exception.UserServiceException;
import user.service.UserService;
import user.util.CommonUtil;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Tag(name = "User Service API")
public class UserRegistrationController {

    private final UserService userService;

    private UserRegistrationController(UserService userService) {
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
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "apiKey", required = true) String apiKey) {

        log.debug("Fetch all Users: [apiKey: {}]", apiKey);

        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
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
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "id of User to be found")
            @PathVariable UUID id) {

        log.debug("Fetch all Users By Id: [apiKey: {}, Id: {}]", apiKey, id);

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
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "signup/sign-in email of User to be found aka loginId")
            @Email(message = "The email address is invalid.", flags = {Pattern.Flag.CASE_INSENSITIVE})
            @PathVariable String email) {

        log.debug("Fetch User By loginId(signup/sign-in Email): [apiKey: {}, email: {}]", apiKey, email);

        UserDTO dto = userService.getUserByLoginId(email);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PostMapping
    @Operation(summary = "Create New User")
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
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "New User Body Content to be created")
            @Valid @RequestBody NewUserDTO newUserDTO) {

        log.debug("Create user: [apiKey: {}, user: {}]", apiKey, newUserDTO.toString());

        newUserDTO.setStatus(UserStatus.NEW_USER);
        UserDTO dto = userService.save(newUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update User password")
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
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "User Password to be updated")
            @Valid @RequestBody UpdatePasswordDTO dto, @PathVariable UUID id) {

        log.debug("Update user password: [apiKey: {}, user: {}]", apiKey, dto.toString());

        if (!CommonUtil.passwordMatch.test(dto.getPassword(), dto.getConfirmPassword())) {
            log.error("User password match failed for id {}", id);
            throw new UserServiceException("password", HttpStatus.BAD_REQUEST, "passwords does not match");
        }
        dto.setId(id);
        UserDTO updated = userService.update(dto);

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
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "User Id to be deleted")
            @PathVariable UUID id) {

        log.debug("Delete user: [apiKey: {}, Id: {}]", apiKey, id);

        UserDTO dto = userService.delete(id);

       return ResponseEntity.status(HttpStatus.OK).body(String.format("{\"id\": \"%s\"}", dto.getId()));
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
