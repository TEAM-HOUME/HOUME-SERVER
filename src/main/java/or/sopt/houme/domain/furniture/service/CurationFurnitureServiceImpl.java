package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.model.entity.CurationFurniture;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.Jjym;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureProductsInfoResponseV2;
import or.sopt.houme.domain.furniture.presentation.dto.response.ProductColorResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.CurationFurnitureRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurationFurnitureServiceImpl implements CurationFurnitureService {

    private final CurationFurnitureRepository curationFurnitureRepository;
    private final RecommendFurnitureRepository recommendFurnitureRepository;
    private final RecommendFurnitureService recommendFurnitureService;
    private final CurationRawProductRepository curationRawProductRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;
    private final JjymRepository jjymRepository;

    @Transactional(readOnly = true)
    @Override
    public List<FurnitureProductsInfoResponse.FurnitureProductInfo> getCurationProducts(
            FurnitureTag furnitureTag,
            CurationSource source
    ) {
        List<CurationFurniture> curations =
                curationFurnitureRepository.findAllByFurnitureTagAndSourceOrderByRankAsc(furnitureTag, source);
        if (curations.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> jjymCountByRecommendFurnitureId = buildJjymCountMap(curations);
        Map<Long, RawProductMeta> rawMetaByProductId = buildRawMetaByProductId(furnitureTag, curations, source);

        return curations.stream()
                .map(curation -> {
                    RecommendFurniture recommendFurniture = curation.getRecommendFurniture();
                    RawProductMeta rawMeta = rawMetaByProductId.get(recommendFurniture.getFurnitureProductId());
                    List<String> colors = rawMeta != null ? rawMeta.colors() : List.of();
                    List<String> clientColors = rawMeta != null ? rawMeta.clientColors() : List.of();

                    return FurnitureProductsInfoResponse.FurnitureProductInfo.of(
                            recommendFurniture.getId(),
                            recommendFurniture.getFurnitureProductImageUrl(),
                            recommendFurniture.getFurnitureProductSiteUrl(),
                            recommendFurniture.getFurnitureProductName(),
                            recommendFurniture.getFurnitureProductMallName(),
                            recommendFurniture.getFurnitureProductId(),
                            curation.getSimilarity(),
                            colors,
                            clientColors,
                            rawMeta != null ? rawMeta.listPrice() : null,
                            rawMeta != null ? rawMeta.discountRate() : null,
                            rawMeta != null ? rawMeta.discountPrice() : null,
                            rawMeta != null ? rawMeta.brandName() : null,
                            jjymCountByRecommendFurnitureId.getOrDefault(recommendFurniture.getId(), 0L)
                    );
                })
                .toList();
    }

    @Transactional
    @Override
    public List<FurnitureProductsInfoResponse.FurnitureProductInfo> saveCurationResults(
            FurnitureTag furnitureTag,
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> infos,
            CurationSource source
    ) {
        if (infos == null || infos.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> idMapByProductId = recommendFurnitureService.saveRecommendFurniture(infos, source);
        LocalDateTime fetchedAt = LocalDateTime.now();
        List<CurationFurniture> curations = new ArrayList<>();

        int rank = 1;
        for (FurnitureProductsInfoResponse.FurnitureProductInfo info : infos) {
            Long recommendFurnitureId = idMapByProductId.get(info.furnitureProductId());
            if (recommendFurnitureId == null) {
                log.warn("큐레이션 저장 실패: recommendFurnitureId 없음, productId={}", info.furnitureProductId());
                continue;
            }

            RecommendFurniture recommendFurniture = recommendFurnitureRepository.getReferenceById(recommendFurnitureId);
            curations.add(CurationFurniture.of(
                    furnitureTag,
                    recommendFurniture,
                    rank,
                    source,
                    info.similarity(),
                    fetchedAt
            ));
            rank++;
        }

        if (curations.isEmpty()) {
            return List.of();
        }

        curationFurnitureRepository.deleteByFurnitureTagAndSource(furnitureTag, source);
        curationFurnitureRepository.saveAll(curations);

        return getCurationProducts(furnitureTag, source);
    }

    @Transactional(readOnly = true)
    @Override
    public FurnitureProductsInfoResponseV2 buildProductsInfoResponse(
            Long userId,
            String userName,
            FurnitureTag furnitureTag,
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> rawInfos
    ) {
        String categoryName = furnitureTag.getFurniture() != null ? furnitureTag.getFurniture().getFurnitureNameKr() : null;
        Map<Long, CurationRawProduct> rawProductByProductId = findLatestRawProductByProductId(furnitureTag, rawInfos);
        Map<Long, List<ProductColorResponse>> colorsByRawProductId = findColorMapByRawProductId(rawProductByProductId);
        Set<Long> likedRecommendIds = findLikedRecommendIds(userId, rawInfos);

        List<FurnitureProductsInfoResponseV2.ProductWrapper> products = rawInfos.stream()
                .map(info -> {
                    CurationRawProduct rawProduct = rawProductByProductId.get(info.furnitureProductId());
                    Long rawProductId = rawProduct != null ? rawProduct.getId() : null;

                    FurnitureProductsInfoResponseV2.ProductInfo product = new FurnitureProductsInfoResponseV2.ProductInfo(
                            rawProductId,
                            info.furnitureProductId(),
                            categoryName,
                            rawProduct != null ? rawProduct.getSource() : CurationSource.RAW.name().toLowerCase(),
                            rawProduct != null ? rawProduct.getBrand() : info.brandName(),
                            rawProduct != null ? rawProduct.getProductName() : info.furnitureProductName(),
                            rawProduct != null ? rawProduct.getProductImageUrl() : info.furnitureProductImageUrl(),
                            rawProduct != null ? rawProduct.getListPrice() : info.listPrice(),
                            rawProduct != null ? rawProduct.getDiscountRate() : info.discountRate(),
                            rawProduct != null ? rawProduct.getDiscountPrice() : info.discountPrice(),
                            rawProduct != null ? rawProduct.getProductMallName() : info.furnitureProductMallName(),
                            rawProduct != null ? rawProduct.getProductSiteUrl() : info.furnitureProductSiteUrl(),
                            rawProductId != null ? colorsByRawProductId.getOrDefault(rawProductId, List.of()) : List.of(),
                            likedRecommendIds.contains(info.id())
                    );
                    return FurnitureProductsInfoResponseV2.ProductWrapper.of(product);
                })
                .toList();

        return FurnitureProductsInfoResponseV2.of(userName, products);
    }

    private Map<Long, Long> buildJjymCountMap(List<CurationFurniture> curations) {
        List<Long> recommendFurnitureIds = curations.stream()
                .map(curation -> curation.getRecommendFurniture().getId())
                .distinct()
                .toList();
        return jjymRepository.countByRecommendFurnitureIds(recommendFurnitureIds);
    }

    private Set<Long> findLikedRecommendIds(Long userId, List<FurnitureProductsInfoResponse.FurnitureProductInfo> rawInfos) {
        List<Long> recommendFurnitureIds = rawInfos.stream()
                .map(FurnitureProductsInfoResponse.FurnitureProductInfo::id)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (recommendFurnitureIds.isEmpty()) {
            return Set.of();
        }

        return jjymRepository.findAllByUserIdAndRecommendFurnitureIdIn(userId, recommendFurnitureIds).stream()
                .map(Jjym::getRecommendFurniture)
                .filter(Objects::nonNull)
                .map(RecommendFurniture::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Map<Long, CurationRawProduct> findLatestRawProductByProductId(
            FurnitureTag furnitureTag,
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> rawInfos
    ) {
        List<Long> productIds = rawInfos.stream()
                .map(FurnitureProductsInfoResponse.FurnitureProductInfo::furnitureProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return Map.of();
        }

        return curationRawProductRepository.findAllByFurnitureTagAndProductIdIn(furnitureTag, productIds).stream()
                .collect(Collectors.toMap(
                        CurationRawProduct::getProductId,
                        rawProduct -> rawProduct,
                        this::selectLatestRawProductForResponse,
                        java.util.LinkedHashMap::new
                ));
    }

    private Map<Long, List<ProductColorResponse>> findColorMapByRawProductId(Map<Long, CurationRawProduct> rawProductByProductId) {
        if (rawProductByProductId.isEmpty()) {
            return Map.of();
        }

        List<Long> rawProductIds = rawProductByProductId.values().stream()
                .map(CurationRawProduct::getId)
                .toList();

        Map<Long, Set<String>> colorNamesByRawProductId = new java.util.LinkedHashMap<>();
        List<CurationRawProductColor> colorEntities = curationRawProductColorRepository.findAllByCurationRawProductIdIn(rawProductIds);
        for (CurationRawProductColor colorEntity : colorEntities) {
            Long rawProductId = colorEntity.getCurationRawProduct().getId();
            if (rawProductId == null) {
                continue;
            }
            String colorName = resolveColorName(colorEntity);
            if (colorName == null) {
                continue;
            }
            colorNamesByRawProductId.computeIfAbsent(rawProductId, key -> new java.util.LinkedHashSet<>())
                    .add(colorName);
        }

        Map<Long, List<ProductColorResponse>> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<Long, Set<String>> entry : colorNamesByRawProductId.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream()
                    .map(ProductColorResponse::fromName)
                    .toList());
        }
        return result;
    }

    private CurationRawProduct selectLatestRawProductForResponse(CurationRawProduct current, CurationRawProduct candidate) {
        LocalDateTime currentFetchedAt = current.getFetchedAt();
        LocalDateTime candidateFetchedAt = candidate.getFetchedAt();

        if (currentFetchedAt == null && candidateFetchedAt == null) {
            if (candidate.getId() != null && current.getId() != null && candidate.getId() > current.getId()) {
                return candidate;
            }
            return current;
        }
        if (currentFetchedAt == null) {
            return candidate;
        }
        if (candidateFetchedAt == null) {
            return current;
        }
        if (candidateFetchedAt.isAfter(currentFetchedAt)) {
            return candidate;
        }
        if (candidateFetchedAt.isEqual(currentFetchedAt)
                && candidate.getId() != null
                && current.getId() != null
                && candidate.getId() > current.getId()) {
            return candidate;
        }
        return current;
    }

    private String resolveColorName(CurationRawProductColor colorEntity) {
        if (colorEntity.getClientColorName() != null && !colorEntity.getClientColorName().isBlank()) {
            return colorEntity.getClientColorName();
        }
        if (colorEntity.getRawColorName() != null && !colorEntity.getRawColorName().isBlank()) {
            return colorEntity.getRawColorName();
        }
        return null;
    }

    private Map<Long, RawProductMeta> buildRawMetaByProductId(
            FurnitureTag furnitureTag,
            List<CurationFurniture> curations,
            CurationSource source
    ) {
        if (source != CurationSource.RAW) {
            return Map.of();
        }

        List<Long> productIds = curations.stream()
                .map(curation -> curation.getRecommendFurniture().getFurnitureProductId())
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return Map.of();
        }

        List<CurationRawProduct> rawProducts =
                curationRawProductRepository.findAllByFurnitureTagAndProductIdIn(furnitureTag, productIds);
        if (rawProducts.isEmpty()) {
            return Map.of();
        }

        Map<Long, CurationRawProduct> rawByProductId = rawProducts.stream()
                .collect(Collectors.toMap(
                        CurationRawProduct::getProductId,
                        raw -> raw,
                        this::selectLatestRawProduct
                ));

        List<Long> rawProductIds = rawProducts.stream()
                .map(CurationRawProduct::getId)
                .toList();
        Map<Long, Long> rawProductIdToProductId = rawProducts.stream()
                .collect(Collectors.toMap(
                        CurationRawProduct::getId,
                        CurationRawProduct::getProductId,
                        (first, second) -> first
                ));

        Map<Long, Set<String>> rawColorsByProductId = new HashMap<>();
        Map<Long, Set<String>> clientColorsByProductId = new HashMap<>();
        List<CurationRawProductColor> colorEntities =
                curationRawProductColorRepository.findAllByCurationRawProductIdIn(rawProductIds);
        for (CurationRawProductColor colorEntity : colorEntities) {
            Long rawProductId = colorEntity.getCurationRawProduct().getId();
            Long productId = rawProductIdToProductId.get(rawProductId);
            if (productId == null) {
                continue;
            }

            String rawColorName = colorEntity.getRawColorName();
            String clientColorName = colorEntity.getClientColorName();

            if (!isBlank(rawColorName)) {
                rawColorsByProductId.computeIfAbsent(productId, key -> new LinkedHashSet<>()).add(rawColorName);
            }
            if (!isBlank(clientColorName)) {
                clientColorsByProductId.computeIfAbsent(productId, key -> new LinkedHashSet<>()).add(clientColorName);
            }
        }

        Map<Long, RawProductMeta> rawMetaByProductId = new HashMap<>();
        for (Map.Entry<Long, CurationRawProduct> entry : rawByProductId.entrySet()) {
            Long productId = entry.getKey();
            CurationRawProduct rawProduct = entry.getValue();
            List<String> colors = new ArrayList<>(rawColorsByProductId.getOrDefault(productId, Set.of()));
            List<String> clientColors = new ArrayList<>(clientColorsByProductId.getOrDefault(productId, Set.of()));
            List<String> colorHexCodes = ColorHexMapper.toHexCodes(colors);
            List<String> clientColorHexCodes = ColorHexMapper.toHexCodes(clientColors);

            rawMetaByProductId.put(productId, new RawProductMeta(
                    colorHexCodes,
                    clientColorHexCodes,
                    rawProduct.getListPrice(),
                    rawProduct.getDiscountRate(),
                    rawProduct.getDiscountPrice(),
                    rawProduct.getBrand()
            ));
        }
        return rawMetaByProductId;
    }

    private CurationRawProduct selectLatestRawProduct(CurationRawProduct current, CurationRawProduct candidate) {
        LocalDateTime currentFetchedAt = current.getFetchedAt();
        LocalDateTime candidateFetchedAt = candidate.getFetchedAt();

        if (currentFetchedAt == null) {
            return candidate;
        }
        if (candidateFetchedAt == null) {
            return current;
        }
        return candidateFetchedAt.isAfter(currentFetchedAt) ? candidate : current;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record RawProductMeta(
            List<String> colors,
            List<String> clientColors,
            Long listPrice,
            Integer discountRate,
            Long discountPrice,
            String brandName
    ) {
    }
}
