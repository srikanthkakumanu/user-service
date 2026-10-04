package user.service;

import user.dto.NewUserDTO;
import user.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO save(NewUserDTO dto);
    UserDTO update(UUID id, NewUserDTO dto);
//  public Iterable<T1> save(Collection<T2> domains);
//  public T1 delete(T2 domain);
    UserDTO delete(UUID id);
    UserDTO findById(UUID id);
    List<UserDTO> findAll();

    UserDTO getUserByLoginId(String email);
    UserDTO lock(UUID id);
    UserDTO unlock(UUID id);
    void resetPassword(UUID id, String temporaryPassword, boolean temporary);
    void assignRole(UUID id, String role);
    void removeRole(UUID id, String role);
}
