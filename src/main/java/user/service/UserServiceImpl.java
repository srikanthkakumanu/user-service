package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import user.domain.User;
import user.dto.*;
import user.exception.UserServiceException;
import user.mapper.UserMapper;
import user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {


    private final UserRepository repository;
    private final UserMapper mapper;

    public UserServiceImpl(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public UserDTO save(NewUserDTO dto) {

        log.debug("save: [{}]", dto.toString());

        Optional<User> found = repository.findByLoginId(dto.getLoginId());

        if (found.isPresent()) {
            log.error("User with id {} already exists", dto.getId());
            throw new UserServiceException("loginId",
                    HttpStatus.CONFLICT, "User already exists");
        }

        UserProfileDTO profile = new UserProfileDTO();
        profile.setEmail(dto.getLoginId());
        dto.setProfile(profile);

        User newUser = mapper.toDomain(dto);

        log.info("Generated Id before saving user: {}", newUser.getId());
        User saved = repository.save(newUser);
        log.info("Generated Id after saving user: {}", saved.getId());

        return mapper.toDTO(saved);
    }

    @Override
    public UserDTO delete(UUID id) {

        log.debug("delete: [Id: {}]", id);

        User found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist");
                });

        repository.delete(found);

        return mapper.toDTO(found);
    }

    @Override
    public UserDTO findById(UUID id) {

        log.debug("findById: [Id: {}]", id);

        User found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist");
                });

        return mapper.toDTO(found);
    }

    @Override
    public List<UserDTO> findAll() {

        log.debug("findAll() called");

        List<User> allUsers = repository.findAll();
        log.debug("getAllUsers: length: {}", allUsers.size());

        return allUsers.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserByLoginId(String email) {

        log.debug("getUserByLoginId: [email: {}]", email);

        User user = repository.findByLoginId(email)
                .orElseThrow(() -> {
                    log.error(String.format("User with signup/sign-in email: %s does not exist", email));
                    return new UserServiceException("loginId",
                            HttpStatus.NOT_FOUND,
                            "User does not exist");
                        }
                );
        return mapper.toDTO(user);
    }
}
