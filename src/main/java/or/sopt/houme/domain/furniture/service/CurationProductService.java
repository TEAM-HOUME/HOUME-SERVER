package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductDetailResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductListResponse;
import or.sopt.houme.domain.user.model.entity.User;

import java.util.List;

public interface CurationProductService {
    CurationProductFilterResponse getFilterMetadata();

    CurationProductListResponse getProducts(
            String keyword,
            List<Long> typeIds,
            List<String> priceRangeIds,
            List<Long> colorIds,
            Long cursor,
            Integer size
    );

    CurationProductListResponse getProductsV2(
            String keyword,
            List<Long> typeIds,
            List<String> priceRangeIds,
            List<Long> colorIds,
            Long cursor,
            Integer size
    );

    CurationProductDetailResponse getProductDetail(Long id, User user);
}
