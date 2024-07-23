package user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.domain.Address;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {}
