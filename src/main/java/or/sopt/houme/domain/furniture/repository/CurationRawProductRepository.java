package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurationRawProductRepository extends JpaRepository<CurationRawProduct, Long> {
    List<CurationRawProduct> findAllBySourceAndCategoryAndProductIdIn(
            String source,
            SoozipCategory category,
            List<Long> productIds
    );

    List<CurationRawProduct> findAllByFurnitureTag(FurnitureTag furnitureTag);
}
