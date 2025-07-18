package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import user.common.enums.UserStatus;
import user.domain.Role;
import user.domain.UserDomain;
import user.dto.*;
import user.exception.UserServiceException;
import user.mapper.UserMapper;
import user.repository.RoleRepository;
import user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository, RoleRepository roleRepository,
                           UserMapper mapper,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = repository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO save(NewUserDTO dto) {

        log.debug("save: [{}]", dto.toString());

        userRepository.findByLoginId(dto.getLoginId())
                .ifPresent(u -> { // Using ifPresent for concise error handling
                    log.error("User with loginId {} already exists", dto.getLoginId());
                    throw new UserServiceException("loginId",
                            HttpStatus.CONFLICT, "User already exists");
                });

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getProfile() == null)
            dto.setProfile(new UserProfileDTO());

        dto.getProfile().setEmail(dto.getLoginId());
        dto.setStatus(UserStatus.NEW_USER);

        UserDomain newUserDomain = mapper.toDomain(dto);

        log.info("Generated Id before saving user: {}", newUserDomain.getId());
        UserDomain saved = userRepository.save(newUserDomain);
        log.info("Generated Id after saving user: {}", saved.getId());

        return mapper.toDTO(saved);
    }

    @Override
    public UserDTO update(UUID id, UpdatePasswordDTO dto) {
        log.debug("update: [id: {}, dto: {}]", id, dto.toString());

        Optional<UserDomain> foundOptional = userRepository.findById(id);

        return foundOptional.map(found -> {
                    found.setPassword(passwordEncoder.encode(dto.getPassword()));
                    return mapper.toDTO(userRepository.save(found));
                })
                .orElseThrow(() -> {
                    log.error("User with id {} not found", dto.getId());
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist");
                });
    }

    @Override
    public UserDTO delete(UUID id) {

        log.debug("delete: [Id: {}]", id);

        UserDomain found = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist");
                });

        userRepository.delete(found);

        return mapper.toDTO(found);
    }

    @Override
    public UserDTO findById(UUID id) {

        log.debug("findById: [Id: {}]", id);

        UserDomain found = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist");
                });

        return mapper.toDTO(found);
    }

    @Override
    public List<UserDTO> findAll() {

        log.debug("findAll() called");

        List<UserDomain> allUserDomains = userRepository.findAll();
        log.debug("allUserDomains: length: {}", allUserDomains.size());

        return allUserDomains.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserByLoginId(String email) {

        log.debug("getUserByLoginId: [email: {}]", email);

        UserDomain userDomain = userRepository.findByLoginId(email)
                .orElseThrow(() -> {
                    log.error("User with signup/sign-in email: {} does not exist", email);
                    return new UserServiceException("loginId",
                            HttpStatus.NOT_FOUND,
                            "User does not exist");
                        }
                );
        return mapper.toDTO(userDomain);
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) {

        UserDomain found = userRepository.findByLoginId(loginId)
                .orElseThrow(() ->
                    new UserServiceException("loginId",
                            HttpStatus.NOT_FOUND,
                            "User with signup/sign-in email: %s does not exist".formatted(loginId))
                );

        return new User(found.getLoginId(),
                found.getPassword(),
                //mapRolesToAuthorities(found.getRoles())
                true,
                true,
                true,
                true,
                mapRolesToAuthorities(List.of(new Role("USER", "Basic User"))));
    }

    private List<SimpleGrantedAuthority> mapRolesToAuthorities(List<Role> roles) {
        return (roles.isEmpty())
                ? (List.of(new SimpleGrantedAuthority("USER")))
                : roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole())).toList();
    }

    // TODO implement a custom AuthenticationProvider interface method authenticate()
    //  instead of UserDetailsService
}
