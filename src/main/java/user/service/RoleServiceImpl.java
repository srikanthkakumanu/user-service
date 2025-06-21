package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import user.domain.Role;
import user.dto.RoleDTO;
import user.exception.UserServiceException;
import user.mapper.RoleMapper;
import user.repository.RoleRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repository;
    private final RoleMapper mapper;

    public RoleServiceImpl(RoleRepository repository, RoleMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public RoleDTO save(UUID id, String description) {
        log.debug("save: [{}, {}]", id, description);

        Role found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Role with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "Role does not exist");
                });

        found.setDescription(description);

        return mapper.toDTO (repository.save(found));
    }

    public RoleDTO save(String role, String description) {
        log.debug("save: [{}, {}]", role, description);

        repository.findByRole(role.trim())
                .ifPresent(r -> {
                    log.error(String.format("Role name %s already exist", role));
                    throw new UserServiceException("role",
                            HttpStatus.CONFLICT,
                            "Role already exists");
                });

        Role saved = repository.save(Role.builder()
                .role(role.trim())
                .description(description.trim()).build());

        log.info("Generated Id after saving role: {}", saved.getId());
        return mapper.toDTO (saved);
    }

    @Override
    public RoleDTO save (RoleDTO dto) {
        log.debug("save: [{}]", dto.toString());

        Role saved =
                repository.findById(dto.getId())
                        .map(found -> {
                            found.setRole(dto.getRole());
                            found.setDescription(dto.getDescription());
                            return repository.save(found);
                        })
                        .orElseThrow(() -> {
                            log.error("Role with id {} not found", dto.getId());
                            return new UserServiceException("id", HttpStatus.NOT_FOUND, "Role does not exist");
                        });
        return mapper.toDTO(saved);
    }

    @Override
    public RoleDTO delete(UUID id) {
        log.debug("delete: [Id: {}]", id);

        Role found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Role with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "Role does not exist");
                });

        repository.delete(found);
        return mapper.toDTO(found);

    }

    @Override
    public List<RoleDTO> findAll() {
        log.debug("findAll() called");

        List<Role> allRoles = repository.findAll();
        log.debug("allRoles: length: {}", allRoles.size());

        return allRoles.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public RoleDTO findById(UUID id) {
        log.debug("findById: [Id: {}]", id);

        Role found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Role with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "Role does not exist");
                });

        return mapper.toDTO(found);
    }

    @Override
    public RoleDTO findByRoleName(String role) {
        log.debug("findByRoleName: [role: {}]", role);

        Role found = repository.findByRole(role.trim())
                .orElseThrow(() -> {
                            log.error(String.format("Role name with %s does not exist", role));
                            return new UserServiceException("role",
                                    HttpStatus.NOT_FOUND,
                                    "Role does not exist");
                        }
                );

       return mapper.toDTO(found);
    }
}
