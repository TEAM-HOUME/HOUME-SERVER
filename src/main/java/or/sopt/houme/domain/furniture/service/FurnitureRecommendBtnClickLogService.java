package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.user.domain.User;

/** 가구추천버튼 클릭로그 기록 인바운드 계약 (#582). */
public interface FurnitureRecommendBtnClickLogService {

    void createFurnitureRecommendBtnClickLog(User user);
}
