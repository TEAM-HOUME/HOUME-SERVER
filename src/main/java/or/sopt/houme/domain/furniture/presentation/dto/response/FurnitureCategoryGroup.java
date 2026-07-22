package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.furniture.domain.FurnitureTypeView;

import java.util.List;

// 가구 카테고리
public record FurnitureCategoryGroup(
        Long categoryId, // FurnitureType의 id
        String nameKr,  // 카테고리 한글명
        String nameEng, // 카테고리 영어명
        List<FurnitureCategoryItem> furnitures
) {

    public static FurnitureCategoryGroup from(FurnitureTypeView furnitureType, List<FurnitureCategoryItem> furnitures) {
        return new FurnitureCategoryGroup(
                furnitureType.id(),
                furnitureType.nameKr(),
                furnitureType.nameEng(),
                furnitures
        );
    }
}
