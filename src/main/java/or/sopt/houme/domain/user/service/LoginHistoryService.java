package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.user.model.entity.LoginType;
import or.sopt.houme.domain.user.model.entity.SocialType;

public interface LoginHistoryService {

    // 로그인 이력 적재 (신규 가입 / 기존 로그인)
    void record(Long userId, LoginType loginType, SocialType socialType);
}
