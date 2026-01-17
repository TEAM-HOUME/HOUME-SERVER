package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendFurnitureRepository extends JpaRepository<RecommendFurniture,Long> {

    boolean existsByFurnitureProductId(Long furnitureProductId);

    java.util.Optional<RecommendFurniture> findByFurnitureProductId(Long furnitureProductId);
}
