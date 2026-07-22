package or.sopt.houme.furniture.domain.port.out;

import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.furniture.domain.RecommendFurniture;

import java.util.List;
import java.util.Optional;

/**
 * 추천가구 영속화 아웃바운드 포트.
 */
public interface RecommendFurniturePort {

    Optional<RecommendFurniture> findById(Long id);

    List<RecommendFurniture> findAllByIdIn(List<Long> ids);

    Optional<RecommendFurniture> findBySourceAndFurnitureProductId(CurationSource source, Long furnitureProductId);

    List<RecommendFurniture> findAllBySourceAndFurnitureProductIdIn(CurationSource source, List<Long> furnitureProductIds);

    RecommendFurniture save(RecommendFurniture recommendFurniture);
}
