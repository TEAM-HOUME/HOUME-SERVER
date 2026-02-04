package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;

import java.util.List;
import java.util.Map;

public interface RecommendFurnitureService {
    Map<Long, Long> saveRecommendFurniture(
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> requestDto,
            CurationSource source
    );
}
