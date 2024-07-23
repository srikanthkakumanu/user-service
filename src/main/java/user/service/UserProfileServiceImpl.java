package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import user.domain.Address;
import user.domain.UserProfile;
import user.dto.AddressDTO;
import user.dto.UserProfileDTO;
import user.exception.UserServiceException;
import user.mapper.AddressMapper;
import user.mapper.UserProfileMapper;
import user.repository.AddressRepository;
import user.repository.UserProfileRepository;
import user.util.AddressMergeFunction;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

@Service
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository repository;
    private final AddressRepository addressRepository;
    private final UserProfileMapper mapper;
    private final AddressMapper addressMapper;

    public UserProfileServiceImpl(UserProfileRepository repository,
                                  UserProfileMapper mapper,
                                  AddressMapper addressMapper,
                                  AddressRepository addressRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.addressMapper = addressMapper;
        this.addressRepository = addressRepository;
    }
// TODO make the below method more functional
    @Override
    public UserProfileDTO save(UserProfileDTO dto) {

        log.debug("save: [{}]", dto.toString());

        UserProfile saved =
                repository.findById(dto.getId())
                        .map(found -> {
                            boolean exist = bothExist.apply(dto.getCurrentAddress(), found.getCurrentAddress());

                            if (exist)
                                found.setCurrentAddress(
                                        addressMerge
                                                .apply(dto.getCurrentAddress(),
                                                        found.getCurrentAddress(),
                                                        addressMapper,
                                                        found.getCurrentAddress()));

                            exist = bothExist.apply(dto.getPermanentAddress(), found.getPermanentAddress());

                            if (exist)
                                found.setPermanentAddress(
                                        addressMerge
                                                .apply(dto.getPermanentAddress(),
                                                        found.getPermanentAddress(),
                                                        addressMapper,
                                                        found.getPermanentAddress()));

                            UserProfile merged = mapper.merge(dto, found);
                            return repository.save(merged);
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

    private BiFunction<AddressDTO, Address, Boolean> bothExist =
            (dto, domain) -> Objects.nonNull(dto) && Objects.nonNull(domain);

    private AddressMergeFunction<AddressDTO, Address, AddressMapper, Address> addressMerge =
            (dto, domain, mapper, result) -> mapper.copyTo(dto, domain);

}

