package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CurationRawProductRepositoryCustom {
    Page<CurationRawProduct> searchByKeyword(String keyword, Pageable pageable);

    Page<CurationRawProduct> findAllByCurationFilters(
            String keyword,
            List<Long> typeIds,
            List<PriceRangeFilter> priceRanges,
            List<String> colorNames,
            Long cursor,
            Pageable pageable
    );

    record PriceRangeFilter(Long min, Long max) {}
}

