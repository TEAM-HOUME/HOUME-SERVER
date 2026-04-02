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

    List<CurationRawProduct> findAllBySourceAndCategoryAndProductIdIn(
            String source,
            SoozipCategory category,
            List<Long> productIds
    );

    List<CurationRawProduct> findAllByProductIdIn(List<Long> productIds);

    @Query("""
            select distinct rawProduct
            from CurationRawProduct rawProduct
            join rawProduct.furnitureTagMappings mapping
            where mapping.furnitureTag = :furnitureTag
              and rawProduct.isExposed = true
            """)
    List<CurationRawProduct> findAllByFurnitureTag(@Param("furnitureTag") FurnitureTag furnitureTag);

    @Query("""
            select distinct rawProduct
            from CurationRawProduct rawProduct
            join rawProduct.furnitureTagMappings mapping
            where mapping.furnitureTag = :furnitureTag
              and rawProduct.productId in :productIds
              and rawProduct.isExposed = true
            """)
    List<CurationRawProduct> findAllByFurnitureTagAndProductIdIn(
            @Param("furnitureTag") FurnitureTag furnitureTag,
            @Param("productIds") List<Long> productIds
    );

    @Query("""
            select distinct rawProduct
            from CurationRawProduct rawProduct
            join rawProduct.furnitureTagMappings mapping
            join mapping.furnitureTag furnitureTag
            join furnitureTag.furniture furniture
            join furniture.furnitureType furnitureType
            where furnitureType.id in :furnitureTypeIds
              and rawProduct.id not in :excludeRawProductIds
              and rawProduct.isExposed = true
            order by rawProduct.id desc
            """)
    List<CurationRawProduct> findAllSimilarByFurnitureTypeIds(
            @Param("furnitureTypeIds") List<Long> furnitureTypeIds,
            @Param("excludeRawProductIds") List<Long> excludeRawProductIds
    );

    @Query("""
            select distinct rawProduct
            from CurationRawProduct rawProduct
            join rawProduct.furnitureTagMappings mapping
            join mapping.furnitureTag furnitureTag
            join furnitureTag.tag tag
            where tag.id in :tagIds
              and rawProduct.id not in :excludeRawProductIds
              and rawProduct.isExposed = true
            order by rawProduct.id desc
            """)
    List<CurationRawProduct> findAllSimilarByTagIds(
            @Param("tagIds") List<Long> tagIds,
            @Param("excludeRawProductIds") List<Long> excludeRawProductIds
    );

    @Query("""
            select rawProduct
            from CurationRawProduct rawProduct
            where rawProduct.brand in :brands
              and rawProduct.id not in :excludeRawProductIds
              and rawProduct.isExposed = true
            order by rawProduct.id desc
            """)
    List<CurationRawProduct> findAllSimilarByBrands(
            @Param("brands") List<String> brands,
            @Param("excludeRawProductIds") List<Long> excludeRawProductIds
    );
}
