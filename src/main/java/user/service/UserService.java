package user.service;

import user.dto.NewUserDTO;
import user.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    public UserDTO save(NewUserDTO dto);
//    public UserDTO update(UserUpdateDTO dto);
//  public Iterable<T1> save(Collection<T2> domains);
//  public T1 delete(T2 domain);
    public UserDTO delete(UUID id);
    public UserDTO findById(UUID id);
    public List<UserDTO> findAll();

    public UserDTO getUserByLoginId(String email);
}
