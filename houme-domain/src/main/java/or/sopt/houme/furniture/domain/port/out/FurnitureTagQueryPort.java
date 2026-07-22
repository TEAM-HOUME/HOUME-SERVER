package or.sopt.houme.furniture.domain.port.out;

import or.sopt.houme.furniture.domain.FurnitureTagView;

import java.util.List;
import java.util.Optional;

/** 가구-스타일태그 매핑 조회 아웃바운드 포트. */
public interface FurnitureTagQueryPort {

    Optional<FurnitureTagView> findByFurnitureIdAndTagId(Long furnitureId, Long tagId);

    List<FurnitureTagView> findAllByTagIdAndFurnitureIdIn(Long tagId, List<Long> furnitureIds);
}
