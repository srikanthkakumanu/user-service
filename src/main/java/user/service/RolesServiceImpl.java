package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user.dto.RoleDTO;
import user.mapper.RolesMapper;
import user.repository.RolesRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class RolesServiceImpl implements RolesService {

    private final RolesRepository repository;
    private final RolesMapper mapper;

    public RolesServiceImpl(RolesRepository repository, RolesMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public RoleDTO save(RoleDTO dto) {
        return null;
    }

    @Override
    public RoleDTO update(RoleDTO dto) {
        return null;
    }

    @Override
    public RoleDTO delete(RoleDTO dto) {
        return null;
    }

    @Override
    public List<RoleDTO> findAll() {
        return List.of();
    }

    @Override
    public RoleDTO findById(UUID id) {
        return null;
    }
}
