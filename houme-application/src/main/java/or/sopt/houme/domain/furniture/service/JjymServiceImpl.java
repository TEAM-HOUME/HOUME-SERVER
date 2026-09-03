package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.dto.CompareCatalogJjymItemResponse;
import or.sopt.houme.compare.application.dto.CompareCatalogJjymListResponse;
import or.sopt.houme.compare.domain.EbayProduct;
import or.sopt.houme.compare.domain.port.out.EbayProductPort;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymItemResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ItemResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ListResponse;
import or.sopt.houme.furniture.domain.CurationRawProductColorView;
import or.sopt.houme.furniture.domain.CurationRawProductView;
import or.sopt.houme.furniture.domain.Jjym;
import or.sopt.houme.furniture.domain.RecommendFurniture;
import or.sopt.houme.furniture.domain.port.out.CurationRawProductQueryPort;
import or.sopt.houme.furniture.domain.port.out.JjymRepositoryPort;
import or.sopt.houme.furniture.domain.port.out.RecommendFurniturePort;
import or.sopt.houme.user.domain.User;
import or.sopt.houme.user.domain.port.out.UserRepositoryPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.CompareException;
import or.sopt.houme.global.api.handler.FurnitureException;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class JjymServiceImpl implements JjymService {

    private final JjymRepositoryPort jjymRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final RecommendFurniturePort recommendFurniturePort;
    private final CurationRawProductQueryPort curationRawProductQueryPort;
    private final EbayProductPort compareCatalogPort;

    @Override
    public boolean jjymToggle(Long userId, Long recommendFurnitureId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        RecommendFurniture furniture = recommendFurniturePort.findById(recommendFurnitureId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        Optional<Jjym> existing = jjymRepositoryPort.findByUserIdAndRecommendFurnitureId(user.getId(), furniture.getId());

        if (existing.isPresent()) {
            jjymRepositoryPort.deleteById(existing.get().getId());
            return false;
        } else {
            jjymRepositoryPort.save(Jjym.of(user.getId(), furniture.getId()));
            return true;
        }
    }

    @Override
    public boolean rawProductJjymToggle(Long userId, Long rawProductId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        RecommendFurniture recommendFurniture = resolveRawProductRecommendFurniture(rawProductId);

        Optional<Jjym> existing = jjymRepositoryPort.findByUserIdAndRecommendFurnitureId(user.getId(), recommendFurniture.getId());
        if (existing.isPresent()) {
            jjymRepositoryPort.deleteById(existing.get().getId());
            return false;
        }

        jjymRepositoryPort.save(Jjym.of(user.getId(), recommendFurniture.getId()));
        return true;
    }

    @Override
    public void likeRawProduct(Long userId, Long rawProductId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        RecommendFurniture recommendFurniture = resolveRawProductRecommendFurniture(rawProductId);

        Optional<Jjym> existing = jjymRepositoryPort.findByUserIdAndRecommendFurnitureId(user.getId(), recommendFurniture.getId());
        if (existing.isPresent()) {
            return;
        }

        jjymRepositoryPort.save(Jjym.of(user.getId(), recommendFurniture.getId()));
    }

    @Transactional(readOnly = true)
    @Override
    public JjymListResponse getMyJjyms(Long userId) {

        List<Jjym> jjyms = jjymRepositoryPort.findAllByUserIdOrderByCreatedAtDesc(userId);
        Map<Long, RecommendFurniture> furnitureById = loadRecommendFurnitureById(jjyms);

        List<JjymItemResponse> items = jjyms.stream()
                .map(j -> furnitureById.get(j.getRecommendFurnitureId()))
                .filter(java.util.Objects::nonNull)
                .map(JjymItemResponse::from)
                .collect(Collectors.toList());

        return JjymListResponse.of(items);

    }

    @Transactional(readOnly = true)
    @Override
    public JjymV2ListResponse getMyRawProductJjyms(Long userId) {
        List<Jjym> jjyms = jjymRepositoryPort.findAllByUserIdOrderByCreatedAtDesc(userId);
        Map<Long, RecommendFurniture> furnitureById = loadRecommendFurnitureById(jjyms);

        List<Jjym> rawProductJjyms = jjyms.stream()
                .filter(jjym -> {
                    RecommendFurniture rf = furnitureById.get(jjym.getRecommendFurnitureId());
                    return rf != null && rf.getSource() == CurationSource.RAW;
                })
                .toList();

        if (rawProductJjyms.isEmpty()) {
            return JjymV2ListResponse.of(List.of());
        }

        Map<Long, CurationRawProductView> rawProductByProductId = buildRawProductByProductId(rawProductJjyms, furnitureById);
        Map<Long, List<String>> colorsByRawProductId = buildColorNamesByRawProductId(rawProductByProductId);
        Map<Long, Long> jjymCountByRecommendFurnitureId = jjymRepositoryPort.countByRecommendFurnitureIds(
                rawProductJjyms.stream()
                        .map(Jjym::getRecommendFurnitureId)
                        .distinct()
                        .toList()
        );

        List<JjymV2ItemResponse> items = rawProductJjyms.stream()
                .map(jjym -> toV2ItemResponse(
                        furnitureById.get(jjym.getRecommendFurnitureId()),
                        rawProductByProductId, colorsByRawProductId, jjymCountByRecommendFurnitureId))
                .toList();

        return JjymV2ListResponse.of(items);
    }

    /** 찜 목록의 추천가구를 일괄 조회해 id 로 매핑한다 (#582: Jjym→RecommendFurniture 연관 절단 대응). */
    private Map<Long, RecommendFurniture> loadRecommendFurnitureById(List<Jjym> jjyms) {
        List<Long> ids = jjyms.stream()
                .map(Jjym::getRecommendFurnitureId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return recommendFurniturePort.findAllByIdIn(ids).stream()
                .collect(Collectors.toMap(RecommendFurniture::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, CurationRawProductView> buildRawProductByProductId(
            List<Jjym> rawProductJjyms, Map<Long, RecommendFurniture> furnitureById) {
        List<Long> productIds = rawProductJjyms.stream()
                .map(jjym -> furnitureById.get(jjym.getRecommendFurnitureId()))
                .filter(java.util.Objects::nonNull)
                .map(RecommendFurniture::getFurnitureProductId)
                .filter(productId -> productId != null)
                .distinct()
                .toList();

        if (productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, CurationRawProductView> rawProductByProductId = new HashMap<>();
        for (CurationRawProductView rawProduct : curationRawProductQueryPort.findAllByProductIdIn(productIds)) {
            rawProductByProductId.merge(
                    rawProduct.getProductId(),
                    rawProduct,
                    this::selectLatestRawProduct
            );
        }
        return rawProductByProductId;
    }

    private Map<Long, List<String>> buildColorNamesByRawProductId(Map<Long, CurationRawProductView> rawProductByProductId) {
        if (rawProductByProductId.isEmpty()) {
            return Map.of();
        }

        List<Long> rawProductIds = rawProductByProductId.values().stream()
                .map(CurationRawProductView::getId)
                .toList();

        Map<Long, Set<String>> colorSetByRawProductId = new HashMap<>();
        for (CurationRawProductColorView color : curationRawProductQueryPort.findColorsByRawProductIdIn(rawProductIds)) {
            Long rawProductId = color.rawProductId();
            if (rawProductId == null) {
                continue;
            }

            String colorName = color.resolveColorName();
            if (colorName == null) {
                continue;
            }

            colorSetByRawProductId.computeIfAbsent(rawProductId, key -> new LinkedHashSet<>()).add(colorName);
        }

        Map<Long, List<String>> colorsByRawProductId = new HashMap<>();
        for (Map.Entry<Long, Set<String>> entry : colorSetByRawProductId.entrySet()) {
            colorsByRawProductId.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return colorsByRawProductId;
    }

    private JjymV2ItemResponse toV2ItemResponse(
            RecommendFurniture recommendFurniture,
            Map<Long, CurationRawProductView> rawProductByProductId,
            Map<Long, List<String>> colorsByRawProductId,
            Map<Long, Long> jjymCountByRecommendFurnitureId
    ) {
        CurationRawProductView rawProduct = rawProductByProductId.get(recommendFurniture.getFurnitureProductId());

        if (rawProduct == null) {
            return JjymV2ItemResponse.of(
                    null,
                    true,
                    recommendFurniture.getFurnitureProductImageUrl(),
                    recommendFurniture.getFurnitureProductSiteUrl(),
                    List.of(),
                    null,
                    recommendFurniture.getFurnitureProductName(),
                    null,
                    null,
                    null,
                    jjymCountByRecommendFurnitureId.getOrDefault(recommendFurniture.getId(), 0L)
            );
        }

        return JjymV2ItemResponse.of(
                rawProduct.getId(),
                true,
                rawProduct.getProductImageUrl(),
                rawProduct.getProductSiteUrl(),
                colorsByRawProductId.getOrDefault(rawProduct.getId(), List.of()),
                rawProduct.getBrand(),
                rawProduct.getProductName(),
                rawProduct.getListPrice(),
                rawProduct.getDiscountRate(),
                rawProduct.getDiscountPrice(),
                jjymCountByRecommendFurnitureId.getOrDefault(recommendFurniture.getId(), 0L)
        );
    }

    private CurationRawProductView selectLatestRawProduct(CurationRawProductView current, CurationRawProductView candidate) {
        LocalDateTime currentFetchedAt = current.getFetchedAt();
        LocalDateTime candidateFetchedAt = candidate.getFetchedAt();

        if (currentFetchedAt == null && candidateFetchedAt == null) {
            return current.getId() != null && candidate.getId() != null && candidate.getId() > current.getId()
                    ? candidate
                    : current;
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
                && current.getId() != null
                && candidate.getId() != null
                && candidate.getId() > current.getId()) {
            return candidate;
        }
        return current;
    }

    @Override
    public boolean catalogJjymToggle(Long userId, Long catalogItemId) {
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        EbayProduct catalogItem = compareCatalogPort.findById(catalogItemId)
                .orElseThrow(() -> new CompareException(ErrorCode.COMPARE_CATALOG_ITEM_NOT_FOUND));

        RecommendFurniture rf = recommendFurniturePort
                .findBySourceAndFurnitureProductId(CurationSource.EBAY, catalogItemId)
                .orElseGet(() -> recommendFurniturePort.save(RecommendFurniture.from(
                        catalogItem.imageUrl(),
                        catalogItem.productUrl(),
                        catalogItem.title(),
                        "eBay",
                        catalogItemId,
                        CurationSource.EBAY
                )));

        Optional<Jjym> existing = jjymRepositoryPort.findByUserIdAndRecommendFurnitureId(userId, rf.getId());
        if (existing.isPresent()) {
            jjymRepositoryPort.deleteById(existing.get().getId());
            return false;
        }
        jjymRepositoryPort.save(Jjym.of(userId, rf.getId()));
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public CompareCatalogJjymListResponse getMyEbayJjyms(Long userId) {
        List<Jjym> jjyms = jjymRepositoryPort.findAllByUserIdOrderByCreatedAtDesc(userId);
        Map<Long, RecommendFurniture> rfById = loadRecommendFurnitureById(jjyms);

        List<CompareCatalogJjymItemResponse> items = jjyms.stream()
                .map(j -> rfById.get(j.getRecommendFurnitureId()))
                .filter(rf -> rf != null && rf.getSource() == CurationSource.EBAY)
                .map(rf -> compareCatalogPort.findById(rf.getFurnitureProductId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(CompareCatalogJjymItemResponse::from)
                .collect(Collectors.toList());

        return CompareCatalogJjymListResponse.of(items);
    }

    private RecommendFurniture resolveRawProductRecommendFurniture(Long rawProductId) {
        CurationRawProductView rawProduct = curationRawProductQueryPort.findById(rawProductId)
                .orElseThrow(() -> new FurnitureException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT));

        return recommendFurniturePort
                .findBySourceAndFurnitureProductId(CurationSource.RAW, rawProduct.getProductId())
                .orElseGet(() -> recommendFurniturePort.save(RecommendFurniture.from(
                        rawProduct.getProductImageUrl(),
                        rawProduct.getProductSiteUrl(),
                        rawProduct.getProductName(),
                        rawProduct.getProductMallName(),
                        rawProduct.getProductId(),
                        CurationSource.RAW
                )));
    }
}
