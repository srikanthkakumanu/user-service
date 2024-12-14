package user.service;

import user.dto.UserProfileDTO;
import java.util.UUID;

public interface UserProfileService {
    public UserProfileDTO save(UserProfileDTO dto);
    public UserProfileDTO delete(UUID id);
    public UserProfileDTO findById(UUID id);
}
