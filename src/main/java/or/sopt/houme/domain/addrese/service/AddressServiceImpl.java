package or.sopt.houme.domain.addrese.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.addrese.dto.request.AddressRequest;
import or.sopt.houme.domain.addrese.entity.Address;
import or.sopt.houme.domain.addrese.repository.AddressRepository;
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

        Address build = Address.builder()
                .user(user)
                .sigungu(addressRequest.sigungu())
                .roadName(addressRequest.roadName())
                .build();

        addressRepository.save(build);
    }
}
