package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationFurniture;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurationFurnitureRepository extends JpaRepository<CurationFurniture, Long> {

    @EntityGraph(attributePaths = "recommendFurniture")
    List<CurationFurniture> findAllByFurnitureTagAndSourceOrderByRankAsc(FurnitureTag furnitureTag, CurationSource source);

    void deleteByFurnitureTagAndSource(FurnitureTag furnitureTag, CurationSource source);
}
