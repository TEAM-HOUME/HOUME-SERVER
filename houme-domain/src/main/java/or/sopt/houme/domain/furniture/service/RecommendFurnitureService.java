package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;

import java.util.List;
import java.util.Map;

public interface RecommendFurnitureService {
    Map<Long, Long> saveRecommendFurniture(List<FurnitureProductsInfoResponse.FurnitureProductInfo> requestDto);
}
