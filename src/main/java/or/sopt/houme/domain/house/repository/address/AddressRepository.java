package or.sopt.houme.domain.house.repository.address;

import or.sopt.houme.domain.house.model.address.entity.Address;
import org.springframework.data.repository.CrudRepository;

public interface AddressRepository extends CrudRepository<Address, Long> {
    void deleteByUserId(Long userId);
}
