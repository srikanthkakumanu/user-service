package user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import user.dto.UserLoginDTO;
import user.service.UserService;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Tag(name = "User Service API")
public class UserLoginController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserLoginController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signin")
    @Operation(summary = "User Login service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User Logged in successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserLoginDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "User failed to login",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User does not exist",
                    content = @Content) })
    public ResponseEntity<?> signin(
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "User Body Content to be used for signin")
            @Valid @RequestBody UserLoginDTO userLoginDTO) {

        log.debug("signin user: [apiKey: {}, user: {}]", apiKey, userLoginDTO.toString());

//        newUserDTO.setStatus(UserStatus.NEW_USER);
//        UserDTO dto = userService.save(newUserDTO);
//        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        // TODO validate user if exists
        // TODO validate the password
        //TODO vaidate the user status
        // TODO perform user login
        return null;
    }

}
