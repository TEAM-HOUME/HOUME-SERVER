package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.dto.CompareCatalogJjymItemResponse;
import or.sopt.houme.compare.application.dto.CompareCatalogJjymListResponse;
import or.sopt.houme.compare.domain.EbayProduct;
import or.sopt.houme.compare.domain.port.out.EbayProductPort;
import or.sopt.houme.coupang.domain.CoupangProduct;
import or.sopt.houme.coupang.domain.port.out.CoupangProductPort;
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
    private final CoupangProductPort coupangProductPort;

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

        List<Jjym> rawJjyms = jjyms.stream()
                .filter(jjym -> {
                    RecommendFurniture rf = furnitureById.get(jjym.getRecommendFurnitureId());
                    return rf != null && rf.getSource() == CurationSource.RAW;
                })
                .toList();

        Map<Long, CurationRawProductView> rawProductByProductId = buildRawProductByProductId(rawJjyms, furnitureById);
        Map<Long, List<String>> colorsByRawProductId = buildColorNamesByRawProductId(rawProductByProductId);
        Map<Long, CoupangProduct> coupangProductById = buildCoupangProductById(jjyms, furnitureById);

        List<Long> allRfIds = jjyms.stream()
                .map(Jjym::getRecommendFurnitureId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Long> jjymCountByRecommendFurnitureId = allRfIds.isEmpty()
                ? Map.of()
                : jjymRepositoryPort.countByRecommendFurnitureIds(allRfIds);

        List<JjymV2ItemResponse> items = jjyms.stream()
                .map(jjym -> {
                    RecommendFurniture rf = furnitureById.get(jjym.getRecommendFurnitureId());
                    if (rf == null) return null;
                    if (rf.getSource() == CurationSource.RAW) {
                        return toV2ItemResponse(rf, rawProductByProductId, colorsByRawProductId, jjymCountByRecommendFurnitureId);
                    }
                    if (rf.getSource() == CurationSource.EBAY) {
                        return toEbayJjymResponse(rf, jjymCountByRecommendFurnitureId);
                    }
                    if (rf.getSource() == CurationSource.COUPANG) {
                        return toCoupangJjymResponse(rf, coupangProductById, jjymCountByRecommendFurnitureId);
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
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
        long jjymCount = jjymCountByRecommendFurnitureId.getOrDefault(recommendFurniture.getId(), 0L);

        if (rawProduct == null) {
            return JjymV2ItemResponse.of(
                    "RAW", null, null, true,
                    recommendFurniture.getFurnitureProductImageUrl(),
                    recommendFurniture.getFurnitureProductSiteUrl(),
                    List.of(), null,
                    recommendFurniture.getFurnitureProductName(),
                    null, null, null, jjymCount
            );
        }

        return JjymV2ItemResponse.of(
                "RAW", rawProduct.getId(), null, true,
                rawProduct.getProductImageUrl(),
                rawProduct.getProductSiteUrl(),
                colorsByRawProductId.getOrDefault(rawProduct.getId(), List.of()),
                rawProduct.getBrand(),
                rawProduct.getProductName(),
                rawProduct.getListPrice(),
                rawProduct.getDiscountRate(),
                rawProduct.getDiscountPrice(),
                jjymCount
        );
    }

    private JjymV2ItemResponse toEbayJjymResponse(
            RecommendFurniture rf,
            Map<Long, Long> jjymCountByRecommendFurnitureId
    ) {
        return JjymV2ItemResponse.of(
                "EBAY", null, rf.getFurnitureProductId(), true,
                rf.getFurnitureProductImageUrl(),
                rf.getFurnitureProductSiteUrl(),
                null, rf.getFurnitureProductMallName(),
                rf.getFurnitureProductName(),
                null, null, null,
                jjymCountByRecommendFurnitureId.getOrDefault(rf.getId(), 0L)
        );
    }

    private Map<Long, CoupangProduct> buildCoupangProductById(
            List<Jjym> jjyms, Map<Long, RecommendFurniture> furnitureById) {
        List<Long> ids = jjyms.stream()
                .map(j -> furnitureById.get(j.getRecommendFurnitureId()))
                .filter(rf -> rf != null && rf.getSource() == CurationSource.COUPANG)
                .map(RecommendFurniture::getFurnitureProductId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return Map.of();
        return coupangProductPort.findAllByIdIn(ids).stream()
                .collect(Collectors.toMap(CoupangProduct::id, Function.identity()));
    }

    private JjymV2ItemResponse toCoupangJjymResponse(
            RecommendFurniture rf,
            Map<Long, CoupangProduct> coupangProductById,
            Map<Long, Long> jjymCountByRecommendFurnitureId
    ) {
        long jjymCount = jjymCountByRecommendFurnitureId.getOrDefault(rf.getId(), 0L);
        CoupangProduct product = coupangProductById.get(rf.getFurnitureProductId());

        if (product == null) {
            return JjymV2ItemResponse.of(
                    "COUPANG", null, rf.getFurnitureProductId(), true,
                    rf.getFurnitureProductImageUrl(), rf.getFurnitureProductSiteUrl(),
                    null, "쿠팡", rf.getFurnitureProductName(),
                    null, null, null, jjymCount
            );
        }

        return JjymV2ItemResponse.of(
                "COUPANG", null, product.id(), true,
                product.imageUrl(), product.productUrl(),
                null, "쿠팡", product.name(),
                product.estimatedOriginalPrice(), product.discountRate(),
                product.currentPrice(), jjymCount
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

    private static final Set<String> SUPPORTED_CATALOG_SOURCES = Set.of("EBAY", "COUPANG", "RAW");

    @Override
    public boolean catalogJjymToggle(Long userId, Long catalogItemId, String source) {
        if (!SUPPORTED_CATALOG_SOURCES.contains(source)) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        if ("COUPANG".equals(source)) {
            CoupangProduct product = coupangProductPort.findById(catalogItemId)
                    .orElseThrow(() -> new CompareException(ErrorCode.COMPARE_CATALOG_ITEM_NOT_FOUND));
            RecommendFurniture rf = recommendFurniturePort
                    .findBySourceAndFurnitureProductId(CurationSource.COUPANG, catalogItemId)
                    .orElseGet(() -> recommendFurniturePort.save(RecommendFurniture.from(
                            product.imageUrl(), product.productUrl(), product.name(),
                            "쿠팡", catalogItemId, CurationSource.COUPANG
                    )));
            return toggleJjym(userId, rf);
        }

        if ("RAW".equals(source)) {
            RecommendFurniture rf = resolveRawProductRecommendFurniture(catalogItemId);
            return toggleJjym(userId, rf);
        }

        // default: EBAY
        EbayProduct catalogItem = compareCatalogPort.findById(catalogItemId)
                .orElseThrow(() -> new CompareException(ErrorCode.COMPARE_CATALOG_ITEM_NOT_FOUND));
        RecommendFurniture rf = recommendFurniturePort
                .findBySourceAndFurnitureProductId(CurationSource.EBAY, catalogItemId)
                .orElseGet(() -> recommendFurniturePort.save(RecommendFurniture.from(
                        catalogItem.imageUrl(), catalogItem.productUrl(), catalogItem.title(),
                        "eBay", catalogItemId, CurationSource.EBAY
                )));
        return toggleJjym(userId, rf);
    }

    private boolean toggleJjym(Long userId, RecommendFurniture rf) {
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
