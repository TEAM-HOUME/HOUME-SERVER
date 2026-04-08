package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.ActivityWithFurnitureResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.user.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface FurnitureService {

    // 주요활동, 가구들 제공
    FurnitureAndActivityResponse getFurnitureAndActivity();

    // 주요활동별 매핑 가구 제공
    List<ActivityWithFurnitureResponse> getActivityFurnitureMappings();

    FurnitureCategoriesResponse getFurnitureCategoriesByStyle(User user, Long imageId, List<String> detectedObjects);

    // 가구 중 침대 ID 조회
    Optional<Long> findBedId(List<Long> furnitureIds);

    FurnitureTag findFurnitureTag(User user, Long imageId, Long categoryId);

    FurnitureTag findFurnitureTagForPlan(Long tagId, Long furnitureId);
}
