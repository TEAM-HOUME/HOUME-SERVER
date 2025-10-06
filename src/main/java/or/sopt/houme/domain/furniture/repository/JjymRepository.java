package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.entity.Jjym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JjymRepository extends JpaRepository<Jjym, Long> {

    boolean existsByUser_IdAndRecommendFurniture_Id(Long userId, Long recommendFurnitureId);

    Optional<Jjym> findByUser_IdAndRecommendFurniture_Id(Long userId, Long recommendFurnitureId);

    void deleteByUser_IdAndRecommendFurniture_Id(Long userId, Long recommendFurnitureId);

    @Query("select j from Jjym j join fetch j.recommendFurniture rf where j.user.id = :userId order by j.createdAt desc")
    List<Jjym> findAllByUserIdWithFurnitureOrderByCreatedAtDesc(@Param("userId") Long userId);
}
