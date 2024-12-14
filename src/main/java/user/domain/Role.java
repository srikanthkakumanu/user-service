package user.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tbl_role")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Role extends BaseEntity {

    @NotNull
    @Size(min = 1, max = 20, message = "role must be between 1 and 20 characters")
    private String role;

    @NotNull
    @Size(min = 1, max = 100, message = "description must be between 1 and 100 characters")
    private String description;
}
