package user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import user.domain.Roles;
import user.dto.RoleDTO;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RolesMapper {

    RoleDTO toDTO (Roles domain);
    Roles toDomain (RoleDTO dto);
}
