package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;

public interface FurnitureService {

    // 주요활동, 가구들 제공
    FurnitureAndActivityResponse getFurnitureAndActivity();
}
