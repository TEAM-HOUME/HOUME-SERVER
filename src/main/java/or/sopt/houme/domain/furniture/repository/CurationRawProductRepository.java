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
public interface CurationRawProductRepository extends JpaRepository<CurationRawProduct, Long> {
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

    @Query("""
            select distinct rawProduct
            from CurationRawProduct rawProduct
            join rawProduct.furnitureTagMappings mapping
            where mapping.furnitureTag = :furnitureTag
            """)
    List<CurationRawProduct> findAllByFurnitureTag(@Param("furnitureTag") FurnitureTag furnitureTag);

    @Query("""
            select distinct rawProduct
            from CurationRawProduct rawProduct
            join rawProduct.furnitureTagMappings mapping
            where mapping.furnitureTag = :furnitureTag
              and rawProduct.productId in :productIds
            """)
    List<CurationRawProduct> findAllByFurnitureTagAndProductIdIn(
            @Param("furnitureTag") FurnitureTag furnitureTag,
            @Param("productIds") List<Long> productIds
    );

    @Query("""
            select rawProduct
            from CurationRawProduct rawProduct
            where (:keyword is null
                    or lower(rawProduct.productName) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(rawProduct.brand, '')) like lower(concat('%', :keyword, '%'))
                    or lower(rawProduct.source) like lower(concat('%', :keyword, '%'))
                    or str(rawProduct.productId) like concat('%', :keyword, '%'))
            order by rawProduct.id desc
            """)
    Page<CurationRawProduct> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
