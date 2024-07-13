package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDTO extends BaseDTO {

    @Valid

    @JsonInclude
    @NotNull(message = "email is mandatory")
    @NotBlank(message = "email is mandatory")
    @Email(message = "The email address is invalid.", flags = {Pattern.Flag.CASE_INSENSITIVE})
    private String email;

    @JsonInclude
    @NotNull(message = "firstName is mandatory")
    @NotBlank(message = "firstName is mandatory")
    @Size(min = 2, max = 20, message = "firstName must be between 2 and 20 characters")
    private String firstName;

    @JsonInclude
    @NotNull(message = "lastName is mandatory")
    @NotBlank(message = "lastName is mandatory")
    @Size(min = 2, max = 20, message = "lastName must be between 2 and 20 characters")
    private String lastName;

    @JsonInclude
    @NotNull(message = "mobile is mandatory")
    @NotBlank(message = "mobile is mandatory")
    @Size(min = 10, max = 10, message = "mobile must be 10 characters")
    @Pattern(regexp = "^\\d{1,10}$", flags = { Pattern.Flag.CASE_INSENSITIVE, Pattern.Flag.MULTILINE }, message = "The mobile number is invalid.")
    private String mobile;

    @JsonInclude
    private AddressDTO permanentAddress;

    @JsonInclude
    private AddressDTO currentAddress;

}
