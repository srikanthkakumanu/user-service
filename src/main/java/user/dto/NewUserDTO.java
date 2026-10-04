package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern.Flag;
import lombok.*;
import lombok.experimental.SuperBuilder;
import user.common.enums.UserAgentType;
import user.common.enums.UserStatus;

import java.util.Set;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String temporaryPassword;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<String> roles;

    private UserProfileDTO profile;

    @JsonInclude
    private UserStatus status;

    @JsonInclude
    @NotNull(message = "userAgentType is mandatory")
    private UserAgentType userAgentType;
}
