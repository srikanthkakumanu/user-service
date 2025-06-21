package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern.Flag;
import lombok.*;
import lombok.experimental.SuperBuilder;
import user.common.enums.UserAgentType;
import user.common.enums.UserStatus;

import java.util.List;

/**
 * Ref: https://medium.com/@tericcabrel/validate-request-body-and-parameter-in-spring-boot-53ca77f97fe9
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class NewUserDTO extends BaseDTO {

    @Valid

    @JsonInclude
    @NotNull(message = "email is mandatory")
    @Email(message = "The email address is invalid.", flags = {Flag.CASE_INSENSITIVE})
    private String loginId;

    @JsonInclude
    @NotNull(message = "password is mandatory")
    @Size(min = 5, max = 20, message = "password must be between 5 and 20 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{5,20}$", message = "password is invalid")
    private String password;

    private UserProfileDTO profile;

    @JsonInclude
    private UserStatus status;

    @JsonInclude
    @NotNull(message = "userAgentType is mandatory")
    private UserAgentType userAgentType;
}
