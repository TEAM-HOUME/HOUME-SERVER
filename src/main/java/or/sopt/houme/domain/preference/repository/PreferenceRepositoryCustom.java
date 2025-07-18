package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.Preference;

import java.util.Optional;

public interface PreferenceRepositoryCustom {
    Optional<Preference> findPreferenceByUserIdAndImageId(Long userId, Long imageId);
}
