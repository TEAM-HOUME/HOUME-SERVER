package or.sopt.houme.domain.house.service.address;

import or.sopt.houme.domain.house.presentation.address.dto.request.AddressRequest;
import or.sopt.houme.domain.user.model.entity.User;

public interface AddressService {

    // 주소 저장하기
    void createAddress(User user, AddressRequest addressRequest);
}
