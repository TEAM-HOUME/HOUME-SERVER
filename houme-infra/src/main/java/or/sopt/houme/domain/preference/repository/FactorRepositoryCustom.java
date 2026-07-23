package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.model.entity.Factor;

import java.util.List;

public interface FactorRepositoryCustom {
    List<Factor> findFactorsByIsLike(boolean isLike);

    // preferenceId로 PreferenceFactor찾고, 있으면 삭제
    void findPreferenceFactorAndDeleteByPreferenceId(Long preferenceId);
}
