package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.Jjym;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymItemResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ItemResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ListResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.FurnitureException;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class JjymServiceImpl implements JjymService {

    private final JjymRepository jjymRepository;
    private final UserRepository userRepository;
    private final RecommendFurnitureRepository recommendFurnitureRepository;
    private final CurationRawProductRepository curationRawProductRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;

    @Override
    public boolean jjymToggle(Long userId, Long recommendFurnitureId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        RecommendFurniture furniture = recommendFurnitureRepository.findById(recommendFurnitureId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        Optional<Jjym> existing = jjymRepository.findByUserIdAndRecommendFurnitureId(user.getId(), furniture.getId());

        if (existing.isPresent()) {
            jjymRepository.delete(existing.get());
            return false;
        } else {
            Jjym jjym = Jjym.of(user, furniture);
            jjymRepository.save(jjym);
            return true;
        }
    }

    @Override
    public boolean rawProductJjymToggle(Long userId, Long rawProductId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        CurationRawProduct rawProduct = curationRawProductRepository.findById(rawProductId)
                .orElseThrow(() -> new FurnitureException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT));

        RecommendFurniture recommendFurniture = recommendFurnitureRepository
                .findBySourceAndFurnitureProductId(CurationSource.RAW, rawProduct.getProductId())
                .orElseGet(() -> recommendFurnitureRepository.save(RecommendFurniture.from(
                        rawProduct.getProductImageUrl(),
                        rawProduct.getProductSiteUrl(),
                        rawProduct.getProductName(),
                        rawProduct.getProductMallName(),
                        rawProduct.getProductId(),
                        CurationSource.RAW
                )));

        Optional<Jjym> existing = jjymRepository.findByUserIdAndRecommendFurnitureId(user.getId(), recommendFurniture.getId());
        if (existing.isPresent()) {
            jjymRepository.delete(existing.get());
            return false;
        }

        jjymRepository.save(Jjym.of(user, recommendFurniture));
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public JjymListResponse getMyJjyms(Long userId) {

        List<Jjym> jjyms = jjymRepository.findAllByUserIdWithFurnitureOrderByCreatedAtDesc(userId);

        List<JjymItemResponse> items = jjyms.stream()
                .map(j -> JjymItemResponse.from(j.getRecommendFurniture()))
                .collect(Collectors.toList());

        return JjymListResponse.of(items);

    }

    @Transactional(readOnly = true)
    @Override
    public JjymV2ListResponse getMyRawProductJjyms(Long userId) {
        List<Jjym> rawProductJjyms = jjymRepository.findAllByUserIdWithFurnitureOrderByCreatedAtDesc(userId).stream()
                .filter(jjym -> jjym.getRecommendFurniture().getSource() == CurationSource.RAW)
                .toList();

        if (rawProductJjyms.isEmpty()) {
            return JjymV2ListResponse.of(List.of());
        }

        Map<Long, CurationRawProduct> rawProductByProductId = buildRawProductByProductId(rawProductJjyms);
        Map<Long, List<String>> colorsByRawProductId = buildColorNamesByRawProductId(rawProductByProductId);
        Map<Long, Long> jjymCountByRecommendFurnitureId = jjymRepository.countByRecommendFurnitureIds(
                rawProductJjyms.stream()
                        .map(jjym -> jjym.getRecommendFurniture().getId())
                        .distinct()
                        .toList()
        );

        List<JjymV2ItemResponse> items = rawProductJjyms.stream()
                .map(jjym -> toV2ItemResponse(jjym, rawProductByProductId, colorsByRawProductId, jjymCountByRecommendFurnitureId))
                .toList();

        return JjymV2ListResponse.of(items);
    }

    private Map<Long, CurationRawProduct> buildRawProductByProductId(List<Jjym> rawProductJjyms) {
        List<Long> productIds = rawProductJjyms.stream()
                .map(jjym -> jjym.getRecommendFurniture().getFurnitureProductId())
                .filter(productId -> productId != null)
                .distinct()
                .toList();

        if (productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, CurationRawProduct> rawProductByProductId = new HashMap<>();
        for (CurationRawProduct rawProduct : curationRawProductRepository.findAllByProductIdIn(productIds)) {
            rawProductByProductId.merge(
                    rawProduct.getProductId(),
                    rawProduct,
                    this::selectLatestRawProduct
            );
        }
        return rawProductByProductId;
    }

    private Map<Long, List<String>> buildColorNamesByRawProductId(Map<Long, CurationRawProduct> rawProductByProductId) {
        if (rawProductByProductId.isEmpty()) {
            return Map.of();
        }

        List<Long> rawProductIds = rawProductByProductId.values().stream()
                .map(CurationRawProduct::getId)
                .toList();

        Map<Long, Set<String>> colorSetByRawProductId = new HashMap<>();
        for (CurationRawProductColor color : curationRawProductColorRepository.findAllByCurationRawProductIdIn(rawProductIds)) {
            Long rawProductId = color.getCurationRawProduct().getId();
            if (rawProductId == null) {
                continue;
            }

            String colorName = resolveColorName(color);
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
            Jjym jjym,
            Map<Long, CurationRawProduct> rawProductByProductId,
            Map<Long, List<String>> colorsByRawProductId,
            Map<Long, Long> jjymCountByRecommendFurnitureId
    ) {
        RecommendFurniture recommendFurniture = jjym.getRecommendFurniture();
        CurationRawProduct rawProduct = rawProductByProductId.get(recommendFurniture.getFurnitureProductId());

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

    private CurationRawProduct selectLatestRawProduct(CurationRawProduct current, CurationRawProduct candidate) {
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

    private String resolveColorName(CurationRawProductColor color) {
        if (color.getClientColorName() != null && !color.getClientColorName().isBlank()) {
            return color.getClientColorName();
        }
        if (color.getRawColorName() != null && !color.getRawColorName().isBlank()) {
            return color.getRawColorName();
        }
        return null;
    }
}
