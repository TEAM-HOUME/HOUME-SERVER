package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.FurnitureType;

import java.util.List;

// 가구 카테고리
public record FurnitureCategoryGroup(
        Long categoryId, // FurnitureType의 id
        String nameKr,  // 카테고리 한글명
        String nameEng, // 카테고리 영어명
        List<FurnitureCategoryItem> furnitures
) {

    public static FurnitureCategoryGroup from(FurnitureType furnitureType, List<FurnitureCategoryItem> furnitures) {
        return new FurnitureCategoryGroup(
                furnitureType.getId(),
                furnitureType.getNameKr(),
                furnitureType.getNameEng(),
                furnitures
        );
    }
}
