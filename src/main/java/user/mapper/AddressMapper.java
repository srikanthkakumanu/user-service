package user.mapper;

import org.mapstruct.*;
import user.domain.Address;
import user.dto.AddressDTO;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AddressMapper {

    AddressDTO toDTO (Address domain);
    Address toDomain (AddressDTO address);

    @Mapping(target = "created", source = "created", qualifiedByName = "convertLocalDateTimeToTimestamp")
    @Mapping(target = "updated", source = "updated", qualifiedByName = "convertLocalDateTimeToTimestamp")
    public Address copyTo(AddressDTO from, @MappingTarget Address to);

    @Named("convertLocalDateTimeToTimestamp")
    default Timestamp convertLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

}
