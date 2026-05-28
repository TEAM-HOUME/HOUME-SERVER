package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurniture;

import java.util.List;

public interface CurationRawProductFurnitureRepositoryCustom {

    List<CurationRawProductFurniture> findAllByCurationRawProductIdInWithFurniture(List<Long> rawProductIds);

    // [pbem22, 2026-05-28, #541] CurationRawProductFurniture 경로 폴백을 위해 추가
    List<Long> findFurnitureIdsHavingProducts(List<Long> furnitureIds);

    // [pbem22, 2026-05-28, #541] furnitureId 기준으로 노출 가능한 원본 상품 매핑 조회
    List<CurationRawProductFurniture> findExposedByFurnitureId(Long furnitureId);
}
