package user.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tbl_user_profile")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UserProfile extends BaseEntity {

    private String email;

    private String firstName;

    private String lastName;

    private String mobile;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "perm_address_id", referencedColumnName = "id", nullable = true)
    private Address permanentAddress;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "current_address_id", referencedColumnName = "id", nullable = true)
    private Address currentAddress;

}
