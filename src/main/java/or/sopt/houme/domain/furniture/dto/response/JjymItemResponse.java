package or.sopt.houme.domain.furniture.dto.response;

import or.sopt.houme.domain.furniture.entity.RecommendFurniture;

public record JjymItemResponse(
        Long id,
        String furnitureProductImageUrl,
        String furnitureProductName,
        Long furnitureProductId
) {
    public static JjymItemResponse from(RecommendFurniture rf) {
        return new JjymItemResponse(
                rf.getId(),
                rf.getFurnitureProductImageUrl(),
                rf.getFurnitureProductName(),
                rf.getFurnitureProductId()
        );
    }
}

