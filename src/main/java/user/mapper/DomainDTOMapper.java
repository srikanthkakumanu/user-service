package user.mapper;


import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import user.dto.BaseDTO;
import user.dto.UserDTO;
import user.domain.User;

@Component
public class DomainDTOMapper {

    private final ModelMapper mapper;

    public DomainDTOMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public UserDTO domainToDTO(User domain) {
        return mapper.map(domain, UserDTO.class);
    }

    public User dtoToDomain(BaseDTO dto) {
        return mapper.map(dto, User.class);
    }
}
