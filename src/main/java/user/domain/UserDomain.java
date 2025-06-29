package user.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import user.common.enums.UserAgentType;
import user.common.enums.UserStatus;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tbl_user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UserDomain extends BaseEntity {

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status")
    private UserStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private UserProfile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_agent_type")
    private UserAgentType userAgentType;

}
