package user.service;

import user.dto.AddressDTO;

import java.util.UUID;

public interface AddressService {
    public AddressDTO save(AddressDTO dto);
    public AddressDTO delete(UUID id);
    public AddressDTO findById(UUID id);
}
