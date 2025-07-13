package or.sopt.houme.domain.preference.service;

import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.preference.entity.Preference;

public interface PromptPreferenceService {

    // 집 프롬프트 좋아요 생성
    void createPromptPreference(House house, Preference preference);
}
