package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.entity.RecommendFurniture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendFurnitureRepository extends JpaRepository<RecommendFurniture,Long> {

    boolean existsByFurnitureProductId(Long furnitureProductId);
}
