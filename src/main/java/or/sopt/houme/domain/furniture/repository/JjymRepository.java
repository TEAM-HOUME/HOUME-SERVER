package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.entity.Jjym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JjymRepository extends JpaRepository<Jjym, Long> {

    boolean existsByUser_IdAndRecommendFurniture_Id(Long userId, Long recommendFurnitureId);

    Optional<Jjym> findByUser_IdAndRecommendFurniture_Id(Long userId, Long recommendFurnitureId);

    void deleteByUser_IdAndRecommendFurniture_Id(Long userId, Long recommendFurnitureId);
}
