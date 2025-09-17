package or.sopt.houme.domain.preference.service;

import or.sopt.houme.domain.house.entity.House;

public interface PromptPreferenceService {

    // 좋아요 or 싫어요
    void togglePromptPreference(House house, boolean isLike);
}
