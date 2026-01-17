package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.Jjym;

import java.util.List;

public interface JjymRepositoryCustom {

    List<Jjym> findAllByUserIdWithFurnitureOrderByCreatedAtDesc(Long userId);

    java.util.Optional<Jjym> findByUserIdAndRecommendFurnitureId(Long userId, Long recommendFurnitureId);
}
