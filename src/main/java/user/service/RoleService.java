package user.service;

import user.dto.RoleDTO;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleDTO save  ( String role, String description);
    RoleDTO save (UUID id, String description);
    RoleDTO save (RoleDTO dto);
    RoleDTO delete (UUID id);
    List<RoleDTO> findAll ();
    RoleDTO findById (UUID id);
    RoleDTO findByRoleName (String role);
}
