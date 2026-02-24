package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.Jjym;

import java.util.List;
import java.util.Map;

public interface JjymRepositoryCustom {

    List<Jjym> findAllByUserIdWithFurnitureOrderByCreatedAtDesc(Long userId);

    java.util.Optional<Jjym> findByUserIdAndRecommendFurnitureId(Long userId, Long recommendFurnitureId);

    Map<Long, Long> countByRecommendFurnitureIds(List<Long> recommendFurnitureIds);
}
