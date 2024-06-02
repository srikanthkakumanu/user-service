package user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor()
@Entity
@Table(name = "users")
public final class User extends BaseEntity {

    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String mobile;

}
