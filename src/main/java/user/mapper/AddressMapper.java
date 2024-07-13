package user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;
import user.domain.Address;
import user.dto.AddressDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressMapper extends BaseMapper {

    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    AddressDTO toDTO (Address domain);
    Address toDomain (AddressDTO address);
}
