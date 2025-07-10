package or.sopt.houme.domain.addrese.service;

import or.sopt.houme.domain.addrese.dto.request.AddressRequest;
import or.sopt.houme.domain.user.entity.User;

public interface AddressService {

    // 주소 저장하기
    void createAddress(User user, AddressRequest addressRequest);
}
