package or.sopt.houme.domain.preference.service;

import or.sopt.houme.domain.preference.dto.response.FactorsResponse;

public interface FactorService {
    FactorsResponse getFactors(boolean isLike);
}
