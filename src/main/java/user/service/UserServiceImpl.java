package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user.domain.User;
import user.dto.NewUserRequestDTO;
import user.dto.UpdateUserRequestDTO;
import user.dto.UserDTO;
import user.mapper.DomainDTOMapper;
import user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {


    private final UserRepository repository;
    private final DomainDTOMapper mapper;

    public UserServiceImpl(UserRepository repository, DomainDTOMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public UserDTO save(NewUserRequestDTO dto) {
        log.debug("save is called");

        User newUser = repository.save(mapper.dtoToDomain(dto));
        log.info("createUser after save id: {}", newUser.getId());

        return mapper.domainToDTO(newUser);
    }

    @Override
    public UserDTO update(UpdateUserRequestDTO dto) {
        log.debug("updateUser Started id: {}", dto.getId());

        User found = repository.findById(dto.getId()).orElse(null);

        if (found == null)
            return null;

        User updated = repository.save(mapper.dtoToDomain(dto));

        return mapper.domainToDTO(updated);
    }

    @Override
    public UserDTO delete(UUID id) {
        log.debug("delete User Started id: {}", id);

        User found = repository.findById(id).orElse(null);

        if (found == null)
            return null;

        repository.delete(found);

        return mapper.domainToDTO(found);
    }

    @Override
    public UserDTO findById(UUID id) {
        User user = repository.findById(id).orElse(null);

        if (user != null)
            return mapper.domainToDTO(user);

        return null;
    }

    @Override
    public List<UserDTO> findAll() {

        log.debug("findAll() called");

        List<User> allUsers = repository.findAll();
        log.debug("getAllUsers: length: {}", allUsers.size());

        return allUsers.stream().map(mapper::domainToDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = repository.findByEmail(email);

        if (user != null)
            return mapper.domainToDTO(user);

        return null;
    }


}
