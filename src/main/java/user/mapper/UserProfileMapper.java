package user.mapper;

import org.mapstruct.*;
import user.domain.UserProfile;
import user.dto.UserProfileDTO;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Mapper (
        uses = { AddressMapper.class },
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserProfileMapper {

    public UserProfileDTO toDTO(UserProfile domain);

    public UserProfile toDomain(UserProfileDTO dto);

    @Mapping(target = "created", source = "created", qualifiedByName = "localDateTimeToTimestamp")
    @Mapping(target = "updated", source = "updated", qualifiedByName = "localDateTimeToTimestamp")
    public UserProfile merge(UserProfileDTO from, @MappingTarget UserProfile to);

    @Named("localDateTimeToTimestamp")
    default Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

}
