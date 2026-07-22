package or.sopt.houme.user.domain.port.out;

import or.sopt.houme.domain.user.model.entity.NicknameWordType;

import java.util.List;

/** 닉네임 단어 조회 아웃바운드 포트 (#582). 단어 텍스트만 반환한다. */
public interface NicknameWordPort {

    List<String> findActiveWordsByType(NicknameWordType type);
}
