package user.service;

import user.dto.RoleDTO;

import java.util.List;
import java.util.UUID;

public interface RolesService {

    public RoleDTO save(RoleDTO dto);
    public RoleDTO update(RoleDTO dto);
    public RoleDTO delete(RoleDTO dto);
    public List<RoleDTO> findAll();
    public RoleDTO findById(UUID id);
}
