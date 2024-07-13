package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import user.common.enums.UserAgentType;
import user.common.enums.UserStatus;
import user.domain.UserProfile;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO extends BaseDTO {

    @JsonInclude
    private String loginId;

    @JsonInclude
    private String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserProfile profile;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserAgentType userAgentType;
}
