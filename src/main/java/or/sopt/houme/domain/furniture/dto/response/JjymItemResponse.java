package or.sopt.houme.domain.furniture.dto.response;

import or.sopt.houme.domain.furniture.entity.RecommendFurniture;

public record JjymItemResponse(
        String furnitureProductImageUrl,
        String furnitureProductName,
        Long furnitureProductId
) {
    public static JjymItemResponse from(RecommendFurniture rf) {
        return new JjymItemResponse(
                rf.getFurnitureProductImageUrl(),
                rf.getFurnitureProductName(),
                rf.getFurnitureProductId()
        );
    }
}

