package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponseForPlan;
import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import or.sopt.houme.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface FurnitureService {

    // 주요활동, 가구들 제공
    FurnitureAndActivityResponse getFurnitureAndActivity();

    FurnitureCategoriesResponse getFurnitureCategoriesByStyle(User user, Long imageId, List<String> detectedObjects);

    // 가구 중 침대 ID 조회
    Optional<Long> findBedId(List<Long> furnitureIds);


    FurnitureTag findFurnitureTag(User user, Long imageId, Long categoryId);

    FurnitureProductsInfoResponseForPlan getFurnitureProductInfoFromNaverApiForPlan(User user, Long tagId, Long furnitureId, String searchKeyword, int searchProductsCount);
}
