package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductDetailResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.user.model.entity.User;

public interface CurationProductService {
    CurationProductFilterResponse getFilterMetadata();

    CurationProductDetailResponse getProductDetail(Long id, User user);
}
