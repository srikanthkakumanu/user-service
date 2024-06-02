package user.service;

import user.dto.UserDTO;

public interface UserService extends CommonService {
    public UserDTO getUserByEmail(String email);
}
