package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.model.entity.LoginHistory;
import or.sopt.houme.domain.user.model.entity.LoginType;
import or.sopt.houme.domain.user.model.entity.SocialType;
import or.sopt.houme.domain.user.repository.LoginHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    // 로그인/가입 트랜잭션과 독립적으로 커밋되도록 REQUIRES_NEW 로 분리한다
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void record(Long userId, LoginType loginType, SocialType socialType) {
        loginHistoryRepository.save(LoginHistory.builder()
                .userId(userId)
                .loginType(loginType)
                .socialType(socialType)
                .build());
    }
}
