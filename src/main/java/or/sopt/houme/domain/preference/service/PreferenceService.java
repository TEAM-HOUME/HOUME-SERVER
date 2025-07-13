package or.sopt.houme.domain.preference.service;

import or.sopt.houme.domain.preference.entity.Preference;

public interface PreferenceService {

    // 좋아요 생성
    Preference createPreference(boolean isLike);
}
