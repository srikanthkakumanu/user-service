package user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;
import user.domain.UserProfile;
import user.dto.UserProfileDTO;

@Mapper ( uses = { AddressMapper.class }, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserProfileMapper extends BaseMapper {

    UserProfileMapper INSTANCE = Mappers.getMapper(UserProfileMapper.class);

    public UserProfileDTO toDTO(UserProfile domain);
    public UserProfile toDomain(UserProfileDTO dto);
}
