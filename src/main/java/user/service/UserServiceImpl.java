package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import user.common.enums.UserStatus;
import user.domain.UserDomain;
import user.dto.*;
import user.exception.UserServiceException;
import user.integration.keycloak.KeycloakUserProvisioningAdapter;
import user.mapper.UserMapper;
import user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final KeycloakUserProvisioningAdapter keycloakAdapter;

    public UserServiceImpl(UserRepository repository,
                           UserMapper mapper,
                           KeycloakUserProvisioningAdapter keycloakAdapter) {
        this.userRepository = repository;
        this.mapper = mapper;
        this.keycloakAdapter = keycloakAdapter;
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

        if (dto.getProfile() == null)
            dto.setProfile(new UserProfileDTO());

        dto.getProfile().setEmail(dto.getLoginId());
        dto.setStatus(UserStatus.NEW_USER);

        String keycloakUserId = keycloakAdapter.createUser(dto);
        UserDomain newUserDomain = mapper.toDomain(dto);
        newUserDomain.setKeycloakUserId(keycloakUserId);

        log.info("Generated Id before saving user: {}", newUserDomain.getId());
        UserDomain saved = userRepository.save(newUserDomain);
        log.info("Generated Id after saving user: {}", saved.getId());
        if (dto.getRoles() != null) {
            dto.getRoles().forEach(role -> keycloakAdapter.assignRealmRole(keycloakUserId, role));
        }

        return mapper.toDTO(saved);
    }

    @Override
    public UserDTO update(UUID id, NewUserDTO dto) {
        log.debug("update: [id: {}, dto: {}]", id, dto.toString());

        Optional<UserDomain> foundOptional = userRepository.findById(id);

        return foundOptional.map(found -> {
                    keycloakAdapter.updateUser(found.getKeycloakUserId(), dto);
                    found.setLoginId(dto.getLoginId());
                    if (dto.getStatus() != null) {
                        found.setStatus(dto.getStatus());
                    }
                    if (dto.getUserAgentType() != null) {
                        found.setUserAgentType(dto.getUserAgentType());
                    }
                    if (dto.getProfile() != null) {
                        found.setProfile(mapper.toDomain(dto).getProfile());
                    }
                    return mapper.toDTO(userRepository.save(found));
                })
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
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

        keycloakAdapter.setAccountEnabled(found.getKeycloakUserId(), false);
        found.setStatus(UserStatus.IN_ACTIVE_USER);

        return mapper.toDTO(userRepository.save(found));
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
    public UserDTO lock(UUID id) {
        UserDomain found = userRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist"));
        keycloakAdapter.setAccountEnabled(found.getKeycloakUserId(), false);
        found.setStatus(UserStatus.IN_ACTIVE_USER);
        return mapper.toDTO(userRepository.save(found));
    }

    @Override
    public UserDTO unlock(UUID id) {
        UserDomain found = userRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist"));
        keycloakAdapter.setAccountEnabled(found.getKeycloakUserId(), true);
        found.setStatus(UserStatus.ACTIVE_USER);
        return mapper.toDTO(userRepository.save(found));
    }

    @Override
    public void resetPassword(UUID id, String temporaryPassword, boolean temporary) {
        UserDomain found = userRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist"));
        keycloakAdapter.resetPassword(found.getKeycloakUserId(), temporaryPassword, temporary);
    }

    @Override
    public void assignRole(UUID id, String role) {
        UserDomain found = userRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist"));
        keycloakAdapter.assignRealmRole(found.getKeycloakUserId(), role);
    }

    @Override
    public void removeRole(UUID id, String role) {
        UserDomain found = userRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("id", HttpStatus.NOT_FOUND, "User does not exist"));
        keycloakAdapter.removeRealmRole(found.getKeycloakUserId(), role);
    }
}
