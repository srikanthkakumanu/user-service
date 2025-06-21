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

        Optional<UserDomain> found = userRepository.findByLoginId(dto.getLoginId());

        // validate if user exists already
        if (found.isPresent()) {
            log.error("User with id {} already exists", dto.getId());
            throw new UserServiceException("loginId",
                    HttpStatus.CONFLICT, "User already exists");
        }
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        UserProfileDTO profile = new UserProfileDTO();
        profile.setEmail(dto.getLoginId());
        dto.setProfile(profile);

        UserDomain newUserDomain = mapper.toDomain(dto);

        log.info("Generated Id before saving user: {}", newUserDomain.getId());
        UserDomain saved = userRepository.save(newUserDomain);
        log.info("Generated Id after saving user: {}", saved.getId());

        return mapper.toDTO(saved);
    }

    @Override
    public UserDTO update(UpdatePasswordDTO dto) {
        log.debug("update: [{}]", dto.toString());

        Optional<UserDomain> foundOptional = userRepository.findById(dto.getId());

        return foundOptional.map(found -> {
                    found.setPassword(
                            passwordEncoder.encode(
                                    "{argon2@SpringSecurity_v5_8}" + dto.getPassword()));
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

        UserDomain found = userRepository.findByLoginId(loginId)
                .orElseThrow(() ->
                    new UserServiceException("loginId",
                            HttpStatus.NOT_FOUND,
                            String.format("User with signup/sign-in email: %s does not exist", loginId))
                );

        return new User(found.getLoginId(),
                found.getPassword(),
                //mapRolesToAuthorities(found.getRoles())
                true,
                true,
                true,
                true,
                new ArrayList<SimpleGrantedAuthority>());
    }

    private List<SimpleGrantedAuthority> mapRolesToAuthorities(List<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole()))
                .toList();
    }

    // TODO implement a custom AuthenticationProvider interface method authenticate()
    //  instead of UserDetailsService
}
