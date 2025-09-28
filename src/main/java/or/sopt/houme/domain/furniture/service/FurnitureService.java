package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.user.entity.User;

import java.util.List;

public interface FurnitureService {

    // 주요활동, 가구들 제공
    FurnitureAndActivityResponse getFurnitureAndActivity();

    FurnitureCategoriesResponse getFurnitureCategoriesByStyle(User user, Long imageId, List<String> detectedObjects);
}
