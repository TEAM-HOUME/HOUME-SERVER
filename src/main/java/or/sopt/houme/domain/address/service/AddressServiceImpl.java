package or.sopt.houme.domain.address.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.address.dto.request.AddressRequest;
import or.sopt.houme.domain.address.entity.Address;
import or.sopt.houme.domain.address.repository.AddressRepository;
import or.sopt.houme.domain.user.entity.User;
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
