package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureProductsInfoResponseV2;
import or.sopt.houme.domain.user.model.entity.User;

import java.util.List;

/**
 * [pbem22, 2026-05-28, #541]
 * CurationRawProductFurniture(직접 매핑) 경로를 통한 가구 상품 조회 서비스.
 * FurnitureTag 경로가 존재하지 않는 가구에 대한 카테고리·상품 폴백을 담당합니다.
 */
public interface CurationRawProductFurnitureService {

    List<Long> getFurnitureIdsHavingProducts(List<Long> furnitureIds);

    FurnitureProductsInfoResponseV2 buildProductsResponseByFurnitureId(User user, Long furnitureId);
}
