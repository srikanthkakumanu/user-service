package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import user.domain.UserProfile;
import user.dto.UserProfileDTO;
import user.exception.UserServiceException;
import user.mapper.AddressMapper;
import user.mapper.UserProfileMapper;
import user.repository.UserProfileRepository;
import static user.util.CommonUtil.*;

import java.util.UUID;

@Service
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository repository;
    private final UserProfileMapper mapper;
    private final AddressMapper addressMapper;


    public UserProfileServiceImpl(UserProfileRepository repository,
                                  UserProfileMapper mapper,
                                  AddressMapper addressMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.addressMapper = addressMapper;
    }

    @Override
    public UserProfileDTO save(UserProfileDTO dto) {

        log.debug("save: [{}]", dto.toString());

        UserProfile saved =
                repository.findById(dto.getId())
                        .map(foundUserProfile -> {
                            UserProfile addressMergedProfile = mergeAddresses(dto, foundUserProfile, addressMapper);
                            UserProfile mergedProfile = mapper.merge(dto, addressMergedProfile);
                            return repository.save(mergedProfile);
                        })
                        .orElseThrow(() -> {
                            log.error("User Profile with id {} not found", dto.getId());
                            return new UserServiceException("id", HttpStatus.NOT_FOUND, "User profile does not exist");
                    });
        return mapper.toDTO(saved);
    }
    
    @Override
    public UserProfileDTO delete(UUID id) {

        log.debug("delete: [Id: {}]", id);

        UserProfile found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("User Profile with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "User profile does not exist");
                });

        repository.delete(found);

        return mapper.toDTO(found);
    }

    @Override
    public UserProfileDTO findById(UUID id) {

        log.debug("findById: [Id: {}]", id);

        UserProfile found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("User Profile with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "User profile does not exist");
                });

        return mapper.toDTO(found);
    }
}

