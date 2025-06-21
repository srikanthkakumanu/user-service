package user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import user.dto.AddressDTO;
import user.dto.UserDTO;
import user.dto.UserProfileDTO;
import user.exception.UserServiceException;
import user.service.AddressService;
import user.service.UserProfileService;
import user.service.UserService;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Tag(name = "User Profiles")
public class UserProfileController {

    private final UserProfileService profileService;
    private final UserService userService;
    private final AddressService addressService;

    private UserProfileController(UserService userService,
                                  UserProfileService profileService,
                                  AddressService addressService) {
        this.userService = userService;
        this.profileService = profileService;
        this.addressService = addressService;
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "Retrieve User Profile by User Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the User matching this User Id",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "User does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Id type mismatch and not valid",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> getUserProfileByUserId(
            @RequestHeader(value = "api-key", required = true) String apiKey,
            @Parameter(description = "id of User to be found")
            @NotNull(message = "user id is mandatory")
            @PathVariable UUID userId) {

        log.debug("Fetch User profile By Id: [api-key: {}, userId: {}]", apiKey, userId);

        UserProfileDTO profile = Optional.of(userService.findById(userId))
                .map(UserDTO::getProfile)
                .orElseThrow(() -> new UserServiceException("id", HttpStatus.NOT_FOUND, "Profile not found for user " + userId));

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/email/{email}/profile")
    @Operation(summary = "Retrieve User Profile by signup/sign-in Email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the User matching this signup/sign-in Email",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "User does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> getUserProfileBySignupEmail(
            @RequestHeader(value = "api-key", required = true) String apiKey,
            @Parameter(description = "signup/sign-in email of User to be found aka loginId")
            @Email(message = "The email address is invalid.", flags = {Pattern.Flag.CASE_INSENSITIVE})
            @PathVariable String email) {

        log.debug("Fetch User Profile By loginId(signup/sign-in Email): [api-key: {}, email: {}]", apiKey, email);

        UserProfileDTO dto = userService.getUserByLoginId(email).getProfile();

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/profile/{id}")
    @Operation(summary = "Retrieve User Profile by Profile Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the User Profile matching this Profile Id",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "User Profile does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Id type mismatch and not valid",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> getUserProfileById(
            @RequestHeader(value = "api-key", required = true) String apiKey,
            @Parameter(description = "id of User profile to be found")
            @NotNull(message = "profile id is mandatory")
            @PathVariable UUID id) {

        log.debug("Fetch User profile By Id: [api-key: {}, Id: {}]", apiKey, id);

        UserProfileDTO dto = profileService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PutMapping("/profile/{id}")
    @Operation(summary = "Update User Profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Updated User Profile",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to Update User Profile",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User Profile Id does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> updateUserProfile(
            @RequestHeader(value = "api-key", required = true) String apiKey,
            @Parameter(description = "id of User Profile to be found")
            @NotNull(message = "profile id is mandatory")
            @PathVariable UUID id,
            @Parameter(description = "User Profile Elements/Body Content to be updated")
            @Valid @RequestBody UserProfileDTO profileDTO) {

        log.debug("Update user: [api-key: {}, user: {}]", apiKey, profileDTO.toString());
        profileDTO.setId(id);
        UserProfileDTO dto = profileService.save(profileDTO);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PutMapping("/profile/address/{id}")
    @Operation(summary = "Update Address of a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Updated Address",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to Update Address",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Address Id does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> updateAddress(
            @RequestHeader(value = "api-key", required = true) String apiKey,
            @Parameter(description = "id of Address to be found")
            @NotNull(message = "address id is mandatory")
            @PathVariable UUID id,
            @Parameter(description = "Address Elements/Body Content to be updated")
            @Valid @RequestBody AddressDTO dto) {

        log.debug("Update address: [api-key: {}, user: {}]", apiKey, dto.toString());
        dto.setId(id);
        AddressDTO updated = addressService.save(dto);

        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @GetMapping("/profile/address/{id}")
    @Operation(summary = "Retrieve Address by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Address matching this Id",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AddressDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "Address does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Id type mismatch and not valid",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> getAddressById(
            @RequestHeader(value = "api-key", required = true) String apiKey,
            @Parameter(description = "id of Address to be found")
            @NotNull(message = "address id is mandatory")
            @PathVariable UUID id) {

        log.debug("Fetch User profile By Id: [api-key: {}, Id: {}]", apiKey, id);

        AddressDTO dto = addressService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<?> constraintViolationException(ConstraintViolationException ex, WebRequest request) {
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
