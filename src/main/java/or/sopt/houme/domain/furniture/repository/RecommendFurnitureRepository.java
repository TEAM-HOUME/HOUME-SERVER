package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendFurnitureRepository extends JpaRepository<RecommendFurniture,Long> {

    boolean existsBySourceAndFurnitureProductId(CurationSource source, Long furnitureProductId);

    java.util.Optional<RecommendFurniture> findBySourceAndFurnitureProductId(CurationSource source, Long furnitureProductId);

    List<RecommendFurniture> findAllBySourceAndFurnitureProductIdIn(CurationSource source, List<Long> furnitureProductIds);
}
