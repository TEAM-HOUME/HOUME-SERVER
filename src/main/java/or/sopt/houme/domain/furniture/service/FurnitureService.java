package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;

import java.util.List;
import java.util.Optional;

public interface FurnitureService {

    // 주요활동, 가구들 제공
    FurnitureAndActivityResponse getFurnitureAndActivity();

    // 가구 중 침대 ID 조회
    Optional<Long> findBedId(List<Long> furnitureIds);
}
