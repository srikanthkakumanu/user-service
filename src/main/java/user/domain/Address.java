package user.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import user.common.enums.AddressType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tbl_user_address")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Address extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private AddressType type;

    private String address1;

    private String address2;

    private String landmark;

    private String area;

    private String city;

    private String zipCode;

    private String state;

    private String country;
}
