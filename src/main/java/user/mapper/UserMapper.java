package user.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import user.domain.User;
import user.dto.NewUserDTO;
import user.dto.UserDTO;

@Mapper ( uses = { UserProfileMapper.class }, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper extends BaseMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    public UserDTO toDTO(User domain);
    public User toDomain(NewUserDTO dto);
}
