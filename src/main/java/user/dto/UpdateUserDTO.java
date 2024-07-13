package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import user.common.enums.UserStatus;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UpdateUserDTO extends BaseDTO {

    @JsonInclude
    @Valid
    @NotNull(message = "email is mandatory")
    @NotBlank(message = "email is mandatory")
    private String email;

    @JsonInclude
    @NotNull(message = "firstName is mandatory")
    @NotBlank(message = "firstName is mandatory")
    @Size(min = 2, max = 20, message = "firstName must be between 2 and 20 characters")
    private String firstName;

    @JsonInclude
    @NotNull(message = "lastName is mandatory")
    @NotBlank(message = "lastName is mandatory")
    @Size(min = 2, max = 20, message = "lastName must be between 2 and 20 characters")
    private String lastName;

    @JsonInclude
    @NotNull(message = "mobile is mandatory")
    @NotBlank(message = "mobile is mandatory")
    @Size(min = 10, max = 10, message = "mobile must be 10 characters")
    private String mobile;

    @JsonInclude
    private UserStatus status;
}
