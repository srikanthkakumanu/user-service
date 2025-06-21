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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import user.dto.*;
import user.service.RoleService;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/roles")
@Slf4j
@Tag(name = "Roles")
public class RoleController {

    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Retrieve All Roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found zero or more Roles",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RoleDTO.class)))}),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content)})
    public ResponseEntity<?> getAllRoles(@RequestHeader(value = "apiKey", required = true) String apiKey) {

        log.debug("Fetch all Roles: [apiKey: {}]", apiKey);

        return ResponseEntity.status(HttpStatus.OK).body(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve Role by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Role matching this Id",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RoleDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "Role does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Id type mismatch and not valid",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> getRoleById(
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "id of Role to be found")
            @NotNull(message = "role id is mandatory")
            @PathVariable UUID id) {

        log.debug("Fetch Role By Id: [apiKey: {}, id: {}]", apiKey, id);

        RoleDTO dto = service.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/{role}")
    @Operation(summary = "Retrieve Role by role name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Role matching this role name",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RoleDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "Role does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> getRoleByName(
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "name of the role to be found aka role")
            @NotNull(message = "role name is mandatory")
            @Size(min = 1, max = 20, message = "role must be between 1 and 20 characters")
            @Pattern(regexp = "^(?=.*[A-Z_]).{1,20}$", message = "role name must be in upper case and _ is allowed")
            @PathVariable String role) {

        log.debug("Fetch Role By Role Name: [apiKey: {}, role: {}]", apiKey, role);

        RoleDTO dto = service.findByRoleName(role);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Updated Role",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RoleDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to Update Role",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Role Id does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> updateRole(
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "id of Role to be found")
            @NotNull(message = "role id is mandatory")
            @PathVariable UUID id,
            @Parameter(description = "Role Elements/Body Content to be updated")
            @Valid @RequestBody RoleDTO roleDTO) {

        log.debug("Update role: [apiKey: {}, role: {}]", apiKey, roleDTO.toString());
        roleDTO.setId(id);
        RoleDTO dto = service.save(roleDTO);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PostMapping
    @Operation(summary = "Creates a Role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RoleDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to create new role",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Role already exists with given role name",
                    content = @Content) })
    public ResponseEntity<?> createRole(
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "new Role Body Content to be created")
            @Valid @RequestBody RoleDTO roleDTO) {

        log.debug("role: [apiKey: {}, role: {}]", apiKey, roleDTO.toString());

        RoleDTO dto = service.save (roleDTO.getRole(), roleDTO.getDescription());

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Deleted Role",
                    content = { @Content() }),
            @ApiResponse(responseCode = "400", description = "Failed to Delete Role",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Role Id does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Authorization Failed",
                    content = @Content) })
    public ResponseEntity<?> deleteUser(
            @RequestHeader(value = "apiKey", required = true) String apiKey,
            @Parameter(description = "Role Id to be deleted")
            @PathVariable UUID id) {

        log.debug("Delete role: [apiKey: {}, Id: {}]", apiKey, id);

        RoleDTO dto = service.delete(id);

        return ResponseEntity.status(HttpStatus.OK).body(String.format("{\"id\": \"%s\"}", dto.getId()));
    }
}
