package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurationRawProductRepository extends JpaRepository<CurationRawProduct, Long>, CurationRawProductRepositoryCustom {
    @Query("""
            select rawProduct
            from CurationRawProduct rawProduct
            where (:category is null or rawProduct.category = :category)
              and (:minListPrice is null or rawProduct.listPrice >= :minListPrice)
              and (:maxListPrice is null or rawProduct.listPrice <= :maxListPrice)
            order by rawProduct.id desc
            """)
    Page<CurationRawProduct> findAllByFilters(
            @Param("category") SoozipCategory category,
            @Param("minListPrice") Long minListPrice,
            @Param("maxListPrice") Long maxListPrice,
            Pageable pageable
    );

    Optional<CurationRawProduct> findBySourceAndCategoryAndProductId(
            String source,
            SoozipCategory category,
            Long productId
    );

    @Query("select rp from CurationRawProduct rp where rp.id = :id and (rp.isExposed = true or rp.isExposed is null)")
    Optional<CurationRawProduct> findByIdAndIsExposedTrueOrNull(@Param("id") Long id);

    List<CurationRawProduct> findAllBySourceAndCategoryAndProductIdIn(
            String source,
            SoozipCategory category,
            List<Long> productIds
    );

    List<CurationRawProduct> findAllByProductIdIn(List<Long> productIds);

    @Query("select rp from CurationRawProduct rp where (rp.isExposed = true or rp.isExposed is null) order by rp.id desc")
    Page<CurationRawProduct> findAllByIsExposedTrueOrNullOrderByIdDesc(Pageable pageable);

    @Query("select rp from CurationRawProduct rp where (rp.isExposed = true or rp.isExposed is null) and rp.productId not in :productIds order by rp.id desc")
    Page<CurationRawProduct> findAllByIsExposedTrueOrNullAndProductIdNotInOrderByIdDesc(@Param("productIds") List<Long> productIds, Pageable pageable);

    @Query("""
            select distinct rawProduct
            from CurationRawProduct rawProduct
            join rawProduct.furnitureTagMappings mapping
            where mapping.furnitureTag = :furnitureTag
              and (rawProduct.isExposed = true or rawProduct.isExposed is null)
            """)
    List<CurationRawProduct> findAllByFurnitureTag(@Param("furnitureTag") FurnitureTag furnitureTag);

    @Query("""
            select distinct rawProduct
            from CurationRawProduct rawProduct
            join rawProduct.furnitureTagMappings mapping
            where mapping.furnitureTag = :furnitureTag
              and rawProduct.productId in :productIds
              and (rawProduct.isExposed = true or rawProduct.isExposed is null)
            """)
    List<CurationRawProduct> findAllByFurnitureTagAndProductIdIn(
            @Param("furnitureTag") FurnitureTag furnitureTag,
            @Param("productIds") List<Long> productIds
    );
}
