package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationFurniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurationFurnitureRepository extends JpaRepository<CurationFurniture, Long> {

    @EntityGraph(attributePaths = "recommendFurniture")
    List<CurationFurniture> findAllByFurnitureTagOrderByRankAsc(FurnitureTag furnitureTag);

    void deleteByFurnitureTag(FurnitureTag furnitureTag);
}
