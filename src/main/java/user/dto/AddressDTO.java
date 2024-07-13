package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.SuperBuilder;
import user.common.enums.AddressType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressDTO extends BaseDTO {


    @JsonInclude
    private AddressType type;

    @JsonInclude
    private String address1;

    @JsonInclude
    private String address2;

    @JsonInclude
    private String landmark;

    @JsonInclude
    private String area;

    @JsonInclude
    private String city;

    @JsonInclude
    private String zipCode;

    @JsonInclude
    private String state;

    @JsonInclude
    private String country;
}
