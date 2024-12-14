package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdatePasswordDTO extends BaseDTO {

    @Valid
    @NotNull(message = "password is mandatory")
    @NotBlank(message = "password must not be empty")
    @Size(min = 5, max = 20, message = "password must be between 5 and 20 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{5,20}$", message = "password is invalid")
    private String password;

    @NotNull(message = "confirmPassword is mandatory")
    @NotBlank(message = "confirmPassword must not be empty")
    @Size(min = 5, max = 20, message = "confirmPassword must be between 5 and 20 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{5,20}$", message = "confirmPassword is invalid")
    private String confirmPassword;

}
