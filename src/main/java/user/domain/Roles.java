package user.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import user.common.enums.AuthRole;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tbl_role")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Roles extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private AuthRole role;

}
