package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoleAssignmentRequest(
        @NotBlank(message = "role is mandatory") String role) {
}
