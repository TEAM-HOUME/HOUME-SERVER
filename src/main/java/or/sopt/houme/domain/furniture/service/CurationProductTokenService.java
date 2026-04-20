package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.model.entity.CurationProductSearchKeyword;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.repository.CurationProductSearchKeywordRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurationProductTokenService {

    private final CurationRawProductRepository curationRawProductRepository;
    private final CurationProductSearchKeywordRepository searchKeywordRepository;
    private final CurationProductTokenizer tokenizer;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void refreshTokensForProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return;

        List<CurationRawProduct> products = curationRawProductRepository.findAllById(productIds);
        Map<Long, List<String>> keywordsByProductId = loadCustomKeywords(productIds);

        for (CurationRawProduct product : products) {
            List<String> furnitureTypeNames = extractFurnitureTypeNames(product);
            List<String> customKeywords = keywordsByProductId.getOrDefault(product.getId(), List.of());

            String tokens = tokenizer.buildTokens(
                    product.getProductName(),
                    product.getBrand(),
                    furnitureTypeNames,
                    customKeywords
            );
            product.updateSearchTokens(tokens);
        }

        curationRawProductRepository.saveAll(products);
    }

    public void initPgTrgm() {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");
            jdbcTemplate.execute(
                    "CREATE INDEX IF NOT EXISTS idx_search_tokens_trgm " +
                    "ON curation_raw_products USING gin (search_tokens gin_trgm_ops)"
            );
            log.info("pg_trgm 인덱스 초기화 완료");
        } catch (Exception e) {
            log.warn("pg_trgm 인덱스 초기화 실패 (권한 부족 또는 이미 존재): {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public long countNullTokenProducts() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM curation_raw_products WHERE search_tokens IS NULL",
                Long.class
        );
        return count != null ? count : 0L;
    }

    @Transactional(readOnly = true)
    public List<Long> findNullTokenProductIds(int limit) {
        return jdbcTemplate.queryForList(
                "SELECT id FROM curation_raw_products WHERE search_tokens IS NULL LIMIT ?",
                Long.class,
                limit
        );
    }

    private List<String> extractFurnitureTypeNames(CurationRawProduct product) {
        return product.getFurnitureTagMappings().stream()
                .map(CurationRawProductFurnitureTag::getFurnitureTag)
                .filter(tag -> tag != null && tag.getFurniture() != null)
                .flatMap(tag -> {
                    List<String> names = new ArrayList<>();
                    Furniture furniture = tag.getFurniture();
                    if (furniture.getFurnitureNameKr() != null) names.add(furniture.getFurnitureNameKr());
                    FurnitureType type = furniture.getFurnitureType();
                    if (type != null && type.getNameKr() != null) names.add(type.getNameKr());
                    return names.stream();
                })
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<Long, List<String>> loadCustomKeywords(List<Long> productIds) {
        List<CurationProductSearchKeyword> keywords = searchKeywordRepository.findAllByCurationRawProductIdIn(productIds);
        Map<Long, List<String>> map = new HashMap<>();
        for (CurationProductSearchKeyword kw : keywords) {
            map.computeIfAbsent(kw.getCurationRawProduct().getId(), k -> new ArrayList<>())
               .add(kw.getKeyword());
        }
        return map;
    }
}
