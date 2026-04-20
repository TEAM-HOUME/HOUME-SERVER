package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface CurationRawProductRepositoryCustom {
    Page<CurationRawProduct> searchByKeyword(String keyword, Pageable pageable);

    Slice<CurationRawProduct> findAllByCurationFilters(
            String keyword,
            List<Long> typeIds,
            List<PriceRangeFilter> priceRanges,
            List<String> colorNames,
            Long cursor,
            Pageable pageable
    );

    Slice<CurationRawProduct> findAllByCurationFiltersV2(
            String keyword,
            List<Long> typeIds,
            List<PriceRangeFilter> priceRanges,
            List<String> colorNames,
            Long cursor,
            Pageable pageable
    );

    Page<CurationRawProduct> findExposedRawProductsExcludingLikedByUser(Long userId, Pageable pageable);

    List<CurationRawProduct> findAllSimilarByFurnitureTypeIds(
            List<Long> furnitureTypeIds,
            List<Long> excludeRawProductIds,
            Pageable pageable
    );

    List<CurationRawProduct> findAllSimilarByTagIds(
            List<Long> tagIds,
            List<Long> excludeRawProductIds,
            Pageable pageable
    );

    List<CurationRawProduct> findAllSimilarByBrands(
            List<String> brands,
            List<Long> excludeRawProductIds,
            Pageable pageable
    );

    record PriceRangeFilter(Long min, Long max) {}
}
