package user.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import user.domain.UserDomain;
import user.dto.NewUserDTO;
import user.dto.UserDTO;

@Mapper (
        uses = { UserProfileMapper.class },
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    //UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    public UserDTO toDTO (UserDomain domain);
    public UserDomain toDomain (NewUserDTO dto);
}
