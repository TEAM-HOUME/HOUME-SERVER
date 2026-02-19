package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.service.dto.CurationRawProductSaveResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurationRawProductService {

    private final CurationRawProductRepository curationRawProductRepository;

    @Transactional(readOnly = true)
    public List<NaverFurnitureProductDto> getCandidatesByFurnitureTag(FurnitureTag furnitureTag) {
        if (furnitureTag == null) {
            return List.of();
        }

        List<CurationRawProduct> rawProducts = curationRawProductRepository.findAllByFurnitureTag(furnitureTag);
        if (rawProducts.isEmpty()) {
            return List.of();
        }

        return rawProducts.stream()
                .filter(product -> product.getProductId() != null)
                .filter(product -> !isBlank(product.getProductImageUrl()))
                .filter(product -> !isBlank(product.getProductSiteUrl()))
                .filter(product -> !isBlank(product.getProductName()))
                .collect(Collectors.toMap(
                        CurationRawProduct::getProductId,
                        product -> new NaverFurnitureProductDto(
                                product.getProductImageUrl(),
                                product.getProductSiteUrl(),
                                product.getProductName(),
                                product.getProductMallName(),
                                product.getProductId()
                        ),
                        (first, second) -> first
                ))
                .values()
                .stream()
                .toList();
    }

    @Transactional
    public CurationRawProductSaveResult saveAll(
            String source,
            SoozipCategory category,
            List<NaverFurnitureProductDto> products
    ) {
        if (products == null || products.isEmpty()) {
            return CurationRawProductSaveResult.empty();
        }

        List<Long> productIds = products.stream()
                .map(NaverFurnitureProductDto::furnitureProductId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, CurationRawProduct> existingById = new HashMap<>();
        if (!productIds.isEmpty()) {
            existingById = curationRawProductRepository.findAllBySourceAndCategoryAndProductIdIn(
                            source,
                            category,
                            productIds
                    )
                    .stream()
                    .collect(Collectors.toMap(CurationRawProduct::getProductId, p -> p));
        }

        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        LocalDateTime fetchedAt = LocalDateTime.now();
        List<CurationRawProduct> toSave = new ArrayList<>();
        Set<Long> seen = new HashSet<>();

        for (NaverFurnitureProductDto dto : products) {
            Long productId = dto.furnitureProductId();
            if (productId == null || !seen.add(productId)) {
                skipped++;
                continue;
            }

            if (isBlank(dto.furnitureProductImageUrl())
                    || isBlank(dto.furnitureProductSiteUrl())
                    || isBlank(dto.furnitureProductName())) {
                skipped++;
                continue;
            }

            CurationRawProduct entity = existingById.get(productId);
            if (entity == null) {
                entity = CurationRawProduct.of(
                        source,
                        category,
                        productId,
                        dto.furnitureProductImageUrl(),
                        dto.furnitureProductSiteUrl(),
                        dto.furnitureProductName(),
                        dto.furnitureProductMallName(),
                        fetchedAt
                );
                inserted++;
            } else {
                entity.updateFrom(
                        dto.furnitureProductImageUrl(),
                        dto.furnitureProductSiteUrl(),
                        dto.furnitureProductName(),
                        dto.furnitureProductMallName(),
                        fetchedAt
                );
                updated++;
            }

            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            curationRawProductRepository.saveAll(toSave);
        }

        return new CurationRawProductSaveResult(inserted, updated, skipped);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
