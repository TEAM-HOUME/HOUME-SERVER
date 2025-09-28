package or.sopt.houme.domain.furniture.dto.response;

import or.sopt.houme.domain.furniture.dto.FurnitureItem;
import or.sopt.houme.domain.furniture.entity.FurnitureType;

import java.util.List;

// 가구 카테고리
public record FurnitureCategoryGroup(
        Long categoryId, // FurnitureType의 id
        String nameKr,  // 카테고리 한글명
        String nameEng, // 카테고리 영어명
        List<FurnitureItem> furnitures
) {

    public static FurnitureCategoryGroup from(FurnitureType furnitureType, List<FurnitureItem> furnitures) {
        return new FurnitureCategoryGroup(
                furnitureType.getId(),
                furnitureType.getNameKr(),
                furnitureType.getNameEng(),
                furnitures
        );
    }
}
