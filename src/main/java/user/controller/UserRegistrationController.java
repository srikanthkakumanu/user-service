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
import org.springframework.web.context.request.WebRequest;
import user.common.enums.UserStatus;
import user.dto.NewUserDTO;
import user.dto.UpdateUserDTO;
import user.dto.UserDTO;
import user.service.UserService;
import static user.util.ValidatorUtil.*;
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
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "apiKey", required = false) String apiKey) {

        log.debug("Fetch all Users: [apiKey: {}]", apiKey);

        ResponseEntity<?> validApiKey = validateApiKey(apiKey);

        if (validApiKey != null)
            return validApiKey;

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
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> getUserById(
            @RequestHeader(value = "apiKey", required = false) String apiKey,
            @Parameter(description = "id of User to be found")
            @PathVariable UUID id) {

        log.debug("Fetch all Users By Id: [apiKey: {}, Id: {}]", apiKey, id);

        ResponseEntity<?> validApiKey = validateApiKey(apiKey);

        if (validApiKey != null)
            return validApiKey;

        UserDTO dto = userService.findById(id);

        if (dto != null)
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Retrieve User by Email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the User matching this Email",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "User Not Found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> getUserByEmail(
            @RequestHeader(value = "apiKey", required = false) String apiKey,
            @Parameter(description = "email of User to be found")
            @Email(message = "The email address is invalid.", flags = {Pattern.Flag.CASE_INSENSITIVE})
            @PathVariable String email) {

        log.debug("Fetch all Users By Email: [apiKey: {}, email: {}]", apiKey, email);

        ResponseEntity<?> validApiKey = validateApiKey(apiKey);

        if (validApiKey != null)
            return validApiKey;

        UserDTO dto = userService.getUserByLoginId(email);

        if (dto != null)
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
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
            @RequestHeader(value = "apiKey", required = false) String apiKey,
            @Parameter(description = "New User Body Content to be created")
            @Valid @RequestBody NewUserDTO newUserRequest) {

        log.debug("Create user: [apiKey: {}, user: {}]", apiKey, newUserRequest.toString());

        ResponseEntity<?> validApiKey = validateApiKey(apiKey);

        if (validApiKey != null)
            return validApiKey;

        newUserRequest.setStatus(UserStatus.NEW_USER);
        UserDTO dto = userService.save(newUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping
    @Operation(summary = "Update User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Updated User",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to Update User",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User Id does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> updateUser(
            @RequestHeader(value = "apiKey", required = false) String apiKey,
            @Parameter(description = "User Elements/Body Content to be updated")
            @Valid @RequestBody UpdateUserDTO updateUserRequest) {

        log.debug("Update user: [apiKey: {}, user: {}]", apiKey, updateUserRequest.toString());

        ResponseEntity<?> validApiKey = validateApiKey(apiKey);

        if (validApiKey != null)
            return validApiKey;

        UserDTO dto = userService.update(updateUserRequest);

        if (dto != null)
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
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
            @RequestHeader(value = "apiKey", required = false) String apiKey,
            @Parameter(description = "User Id to be deleted")
            @PathVariable UUID id) {

        log.debug("Delete user: [apiKey: {}, Id: {}]", apiKey, id);

        ResponseEntity<?> validApiKey = validateApiKey(apiKey);

        if (validApiKey != null)
            return validApiKey;

        UserDTO dto = userService.delete(id);

        if (dto != null)
            return ResponseEntity.status(HttpStatus.OK).body("{}");
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
    }

//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    private Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
//
//        Map<String, String> errors = new HashMap<>();
//
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        return errors;
//    }

    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<?> constraintViolationException(ConstraintViolationException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();

        ex.getConstraintViolations().forEach(cv -> errors.add(cv.getMessage()));

        Map<String, List<String>> result = new HashMap<>();

        result.put("errors", errors);

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

//    public ResponseEntity<?> validateApiKey(String apiKey) {
//
//        final String BAD_API_KEY = "{\"status\":\"Authorization Failed\",\"message\":\"Invalid API Key\"}";
//
//        if (apiKey == null || !API_KEYS.contains(apiKey)) {
//            // return invalid key
//            log.error("Invalid API Key");
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BAD_API_KEY);
//        }
//        return null;
//    }

}
