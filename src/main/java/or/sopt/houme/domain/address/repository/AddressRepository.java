package or.sopt.houme.domain.address.repository;

import or.sopt.houme.domain.address.entity.Address;
import org.springframework.data.repository.CrudRepository;

public interface AddressRepository extends CrudRepository<Address, Long> {
    void deleteByUserId(Long userId);
}
