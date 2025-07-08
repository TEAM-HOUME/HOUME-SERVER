package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MyPageInfoResponse getMyPageInfo(User user) {
        String name = userRepository.findNameById(user.getId());
        long creditCount = userRepository.countByMemberIdAndStatus(user.getId());
        return MyPageInfoResponse.of(name, creditCount);
    }
}
