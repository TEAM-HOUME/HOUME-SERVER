package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;

import java.util.List;

public interface RecommendFurnitureService {
    void saveRecommendFurniture(List<FurnitureProductsInfoResponse.FurnitureProductInfo> requestDto);
}
