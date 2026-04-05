package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.Jjym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JjymRepository extends JpaRepository<Jjym, Long>, JjymRepositoryCustom {
    void deleteByUserId(Long userId);

    boolean existsByUserIdAndRecommendFurnitureId(Long userId, Long recommendFurnitureId);

    List<Jjym> findAllByUserIdAndRecommendFurnitureIdIn(Long userId, List<Long> recommendFurnitureIds);
}
