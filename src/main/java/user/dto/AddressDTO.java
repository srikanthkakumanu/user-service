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


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AddressType type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String address1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String address2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String landmark;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String area;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String city;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String zipCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String state;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String country;
}
