package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import user.domain.Role;
import user.domain.UserDomain;
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
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository,
                           UserMapper mapper,
                           PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO save(NewUserDTO dto) {

        log.debug("save: [{}]", dto.toString());

        Optional<UserDomain> found = repository.findByLoginId(dto.getLoginId());

        if (found.isPresent()) {
            log.error("User with id {} already exists", dto.getId());
            throw new UserServiceException("loginId",
                    HttpStatus.CONFLICT, "User already exists");
        }

        dto.setPassword(
                passwordEncoder.encode(
                        "{argon2@SpringSecurity_v5_8}"
                                + dto.getPassword()));

        UserProfileDTO profile = new UserProfileDTO();
        profile.setEmail(dto.getLoginId());
        dto.setProfile(profile);

        UserDomain newUserDomain = mapper.toDomain(dto);

        log.info("Generated Id before saving user: {}", newUserDomain.getId());
        UserDomain saved = repository.save(newUserDomain);
        log.info("Generated Id after saving user: {}", saved.getId());

        return mapper.toDTO(saved);
    }

    @Override
    public UserDTO update(UpdatePasswordDTO dto) {
        log.debug("update: [{}]", dto.toString());

        Optional<UserDomain> foundOptional = repository.findById(dto.getId());

        return foundOptional.map(found -> {
                    found.setPassword(
                            passwordEncoder.encode(
                                    "{argon2@SpringSecurity_v5_8}" + dto.getPassword()));
                    return mapper.toDTO(repository.save(found));
                })
                .orElseThrow(() -> {
                    log.error("User with id {} not found", dto.getId());
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist");
                });
    }

    @Override
    public UserDTO delete(UUID id) {

        log.debug("delete: [Id: {}]", id);

        UserDomain found = repository.findById(id)
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

        UserDomain found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist");
                });

        return mapper.toDTO(found);
    }

    @Override
    public List<UserDTO> findAll() {

        log.debug("findAll() called");

        List<UserDomain> allUserDomains = repository.findAll();
        log.debug("allUserDomains: length: {}", allUserDomains.size());

        return allUserDomains.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserByLoginId(String email) {

        log.debug("getUserByLoginId: [email: {}]", email);

        UserDomain userDomain = repository.findByLoginId(email)
                .orElseThrow(() -> {
                    log.error(String.format("User with signup/sign-in email: %s does not exist", email));
                    return new UserServiceException("loginId",
                            HttpStatus.NOT_FOUND,
                            "User does not exist");
                        }
                );
        return mapper.toDTO(userDomain);
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) {

        UserDomain found = repository.findByLoginId(loginId)
                .orElseThrow(() ->
                    new UserServiceException("loginId",
                            HttpStatus.NOT_FOUND,
                            String.format("User with signup/sign-in email: %s does not exist", loginId))
                );

        return new User(found.getLoginId(),
                found.getPassword(),
                mapRolesToAuthorities(found.getRoles()));
    }

    private List<SimpleGrantedAuthority> mapRolesToAuthorities(List<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole()))
                .toList();
    }

    // TODO implement a custom AuthenticationProvider interface method authenticate()
    //  instead of UserDetailsService
}
