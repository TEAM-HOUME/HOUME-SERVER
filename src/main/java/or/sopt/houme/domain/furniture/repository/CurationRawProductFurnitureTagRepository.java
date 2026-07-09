package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurationRawProductFurnitureTagRepository extends JpaRepository<CurationRawProductFurnitureTag, Long> {

    @Query("""
            select mapping
            from CurationRawProductFurnitureTag mapping
            join fetch mapping.furnitureTag furnitureTag
            join fetch furnitureTag.furniture furniture
            left join fetch furnitureTag.tag tag
            where mapping.curationRawProduct.id in :rawProductIds
            """)
    List<CurationRawProductFurnitureTag> findAllByCurationRawProductIdInWithFurnitureTag(
            @Param("rawProductIds") List<Long> rawProductIds
    );

    Optional<CurationRawProductFurnitureTag> findByIdAndCurationRawProductId(Long id, Long curationRawProductId);

    boolean existsByCurationRawProductAndFurnitureTag(CurationRawProduct curationRawProduct, FurnitureTag furnitureTag);
}
