package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PasswordResetRequest(
        @NotBlank(message = "temporaryPassword is mandatory") String temporaryPassword,
        boolean temporary) {
}
