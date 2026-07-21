package or.sopt.houme.domain.preference.service;

import or.sopt.houme.domain.preference.presentation.dto.response.FactorsResponse;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;

public interface FactorService {
    FactorsResponse getFactors(boolean isLike);

    void toggleFactorLog(UserJpaEntity user, Long imageId, Long factorId);

    // Preference로 PreferenceFactor 삭제
    void deletePreferenceFactor(Long preferenceId);
}
