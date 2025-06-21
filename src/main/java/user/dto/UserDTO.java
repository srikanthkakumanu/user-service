package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import user.common.enums.UserAgentType;
import user.common.enums.UserStatus;
import user.domain.UserProfile;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO extends BaseDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String loginId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserProfileDTO profile;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserAgentType userAgentType;
}
