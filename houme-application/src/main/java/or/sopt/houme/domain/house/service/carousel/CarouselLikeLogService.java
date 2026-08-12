package or.sopt.houme.domain.house.service.carousel;

import or.sopt.houme.user.domain.User;

/** 캐러셀 좋아요/싫어요 로그 기록 인바운드 계약 (#582). */
public interface CarouselLikeLogService {

    void createLikeLog(User user, Long rawProductId);

    void createHateLog(User user, Long rawProductId);
}
