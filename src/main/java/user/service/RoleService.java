package user.service;

import user.dto.RoleDTO;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    public RoleDTO save ( String role, String description);
    public RoleDTO delete (UUID id);
    public List<RoleDTO> findAll();
    public RoleDTO findById (UUID id);
    public RoleDTO findByRoleName (String role);
}
