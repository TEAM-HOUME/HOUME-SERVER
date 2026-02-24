package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendFurnitureRepository extends JpaRepository<RecommendFurniture,Long> {

    boolean existsBySourceAndFurnitureProductId(CurationSource source, Long furnitureProductId);

    java.util.Optional<RecommendFurniture> findBySourceAndFurnitureProductId(CurationSource source, Long furnitureProductId);
}
