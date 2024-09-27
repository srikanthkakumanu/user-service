package user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import user.domain.Address;
import user.dto.AddressDTO;
import user.exception.UserServiceException;
import user.mapper.AddressMapper;
import user.repository.AddressRepository;
import static user.util.CommonUtil.*;

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

        Optional<Address> foundAddrOptional = repository.findById(dto.getId());

        return foundAddrOptional.map(found -> mergeAddresses(dto, found, mapper))
                .map(repository::save)
                .map(mapper::toDTO)
                .orElseThrow(() -> {
                    log.error("Address with id {} not found", dto.getId());
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "Address does not exist");
                });
    }

    @Override
    public AddressDTO delete(UUID id) {

        log.debug("delete: [Id: {}]", id);

        Address found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Address with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "Address does not exist");
                });

        repository.delete(found);

        return mapper.toDTO(found);

    }

    @Override
    public AddressDTO findById(UUID id) {
        log.debug("findById: [Id: {}]", id);

        Address found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Address with id {} not found", id);
                    return new UserServiceException("id", HttpStatus.NOT_FOUND, "Address does not exist");
                });

        return mapper.toDTO(found);
    }
}
