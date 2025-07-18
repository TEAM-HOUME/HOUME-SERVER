package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.Preference;

public interface PreferenceRepositoryCustom {
    Preference findPreferenceByUserIdAndImageId(Long userId, Long imageId);
}
