package user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import user.domain.Role;
import user.dto.RoleDTO;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RoleMapper {

    RoleDTO toDTO(Role domain);
    @Mapping(target = "role", source = "role")
    Role toDomain(RoleDTO dto);
}
