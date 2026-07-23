package or.sopt.houme.domain.preference.service;

import or.sopt.houme.domain.preference.presentation.dto.response.FactorsResponse;
import or.sopt.houme.user.domain.User;

public interface FactorService {
    FactorsResponse getFactors(boolean isLike);

    void toggleFactorLog(User user, Long imageId, Long factorId);

    // Preference로 PreferenceFactor 삭제
    void deletePreferenceFactor(Long preferenceId);
}
