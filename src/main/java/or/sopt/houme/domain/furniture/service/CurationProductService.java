package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductDetailResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;

public interface CurationProductService {
    CurationProductFilterResponse getFilterMetadata();

    CurationProductDetailResponse getProductDetail(Long id);
}
