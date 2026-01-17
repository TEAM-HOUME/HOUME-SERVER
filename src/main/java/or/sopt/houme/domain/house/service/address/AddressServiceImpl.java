package or.sopt.houme.domain.house.service.address;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.presentation.address.dto.request.AddressRequest;
import or.sopt.houme.domain.house.model.address.entity.Address;
import or.sopt.houme.domain.house.repository.address.AddressRepository;
import or.sopt.houme.domain.user.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    // 주소 등록하기
    @Transactional
    @Override
    public void createAddress(User user, AddressRequest addressRequest) {

        Address address = Address.create(user, addressRequest);

        addressRepository.save(address);
    }
}
