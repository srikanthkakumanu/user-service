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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import user.dto.NewUserDTO;
import user.dto.UserDTO;
import user.dto.UserLoginDTO;
import user.service.UserService;
import user.util.JWTGenerator;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Tag(name = "Users")
public class UserAuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JWTGenerator jwtGenerator;

    public UserAuthController(UserService userService,
                              AuthenticationManager authManager,
                              JWTGenerator jwtGenerator) {
        this.userService = userService;
        this.authManager = authManager;
        this.jwtGenerator = jwtGenerator;
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("Pong");
    }

    @PostMapping("/signin")
    @Operation(summary = "User Sign-In")
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
            @RequestHeader(value = "api-key", required = true) String apiKey,
            @Parameter(description = "User Body Content to be used for login")
            @Valid @RequestBody UserLoginDTO userLoginDTO) {

        log.debug("login user: [api-key: {}, user: {}]", apiKey, userLoginDTO.toString());

        // user signin functionality
        Authentication auth =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                userLoginDTO.getLoginId(),
                                userLoginDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(auth);

        String token = jwtGenerator.generateToken(auth);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return ResponseEntity.ok()
                .headers(headers)
                .body(userLoginDTO.getLoginId());
    }

    @PostMapping("/signup")
    @Operation(summary = "Register/Sign-up/Create New User")
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
            @RequestHeader(value = "api-key", required = true) String apiKey,
            @Parameter(description = "New User Body Content to be created")
            @Valid @RequestBody NewUserDTO newUserDTO) {

        log.debug("Create user: [api-key: {}, user: {}]", apiKey, newUserDTO.toString());

        UserDTO dto = userService.save(newUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

}
