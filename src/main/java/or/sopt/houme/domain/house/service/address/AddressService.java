package or.sopt.houme.domain.house.service.address;

import or.sopt.houme.domain.house.presentation.address.dto.request.AddressRequest;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;

public interface AddressService {

    // 주소 저장하기
    void createAddress(UserJpaEntity user, AddressRequest addressRequest);
}
