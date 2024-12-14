package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude
public class RoleDTO extends BaseDTO {

    @Valid

    @NotNull(message = "role name is mandatory")
    @Size(min = 1, max = 20, message = "role must be between 1 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z_]).{1,20}$", message = "role name must be in upper case and _ is allowed")
    private String role;

    @Size(min = 1, max = 100, message = "description must be between 1 and 100 characters")
    private String description;
}
