package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.model.entity.CurationFurniture;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
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

    private Map<Long, Long> buildJjymCountMap(List<CurationFurniture> curations) {
        List<Long> recommendFurnitureIds = curations.stream()
                .map(curation -> curation.getRecommendFurniture().getId())
                .distinct()
                .toList();
        return jjymRepository.countByRecommendFurnitureIds(recommendFurnitureIds);
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
