package or.sopt.houme.domain.preference.service;

import or.sopt.houme.domain.preference.dto.response.FactorsResponse;
import or.sopt.houme.domain.user.entity.User;

import java.util.List;

public interface FactorService {
    FactorsResponse getFactors(boolean isLike);

    void toggleFactorLog(User user, Long imageId, Long factorId);
}
