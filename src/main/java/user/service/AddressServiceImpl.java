package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import user.domain.Address;
import user.domain.User;
import user.dto.AddressDTO;
import user.dto.UserProfileDTO;
import user.exception.UserServiceException;
import user.mapper.AddressMapper;
import user.repository.AddressRepository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository repository;
    private final AddressMapper mapper;

    public AddressServiceImpl(AddressRepository repository, AddressMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AddressDTO save(AddressDTO dto) {

        log.debug("save: [{}]", dto.toString());

//        Optional<Address> found = repository.findById(dto.getId());
//
//        if (found.isPresent())
//            mapper.toDomain(dto);
//
//            throw new UserServiceException("loginId",
//                    HttpStatus.CONFLICT,
//                    String.format("User with %s already exists", dto.getLoginId()));
//
//        UserProfileDTO profile = new UserProfileDTO();
//        profile.setEmail(dto.getLoginId());
//        dto.setProfile(profile);
//
//        User newUser = mapper.toDomain(dto);
//
//        log.info("Generated Id before saving user: {}", newUser.getId());
//        User saved = repository.save(newUser);
//        log.info("Generated Id after saving user: {}", saved.getId());
//
//        return mapper.toDTO(saved);
        return null;
    }

    @Override
    public AddressDTO delete(UUID id) {
        return null;
    }

    @Override
    public AddressDTO findById(UUID id) {
        return null;
    }

    @Override
    public AddressDTO copy(AddressDTO from, AddressDTO to) {

        return null;
    }
}
