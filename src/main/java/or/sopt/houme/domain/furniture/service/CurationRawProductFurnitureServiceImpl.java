package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurniture;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.Jjym;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureProductsInfoResponseV2;
import or.sopt.houme.domain.furniture.presentation.dto.response.ProductColorResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductFurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [pbem22, 2026-05-28, #541]
 * CurationRawProductFurniture(직접 매핑) 경로를 통한 가구 상품 조회 구현체.
 * FurnitureTag 경로가 없는 가구에 대한 카테고리·상품 폴백을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurationRawProductFurnitureServiceImpl implements CurationRawProductFurnitureService {

    private final CurationRawProductFurnitureRepository curationRawProductFurnitureRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;
    private final RecommendFurnitureRepository recommendFurnitureRepository;
    private final JjymRepository jjymRepository;
    private final FurnitureRepository furnitureRepository;

    @Override
    public List<Long> getFurnitureIdsHavingProducts(List<Long> furnitureIds) {
        return curationRawProductFurnitureRepository.findFurnitureIdsHavingProducts(furnitureIds);
    }

    @Override
    public FurnitureProductsInfoResponseV2 buildProductsResponseByFurnitureId(User user, Long furnitureId) {
        Furniture furniture = furnitureRepository.findById(furnitureId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        List<CurationRawProductFurniture> mappings =
                curationRawProductFurnitureRepository.findExposedByFurnitureId(furnitureId);

        if (mappings.isEmpty()) {
            return FurnitureProductsInfoResponseV2.of(
                    user != null ? user.getName() : null,
                    List.of()
            );
        }

        List<CurationRawProduct> rawProducts = mappings.stream()
                .map(CurationRawProductFurniture::getCurationRawProduct)
                .toList();

        List<Long> rawProductIds = rawProducts.stream()
                .map(CurationRawProduct::getId)
                .toList();

        Map<Long, List<ProductColorResponse>> colorsByRawProductId = buildColorMap(rawProductIds);
        Set<Long> likedRawProductIds = findLikedRawProductIds(user, rawProducts);

        String categoryName = furniture.getFurnitureNameKr();
        String userName = user != null ? user.getName() : null;

        List<FurnitureProductsInfoResponseV2.ProductWrapper> products = rawProducts.stream()
                .map(raw -> {
                    List<ProductColorResponse> colors =
                            colorsByRawProductId.getOrDefault(raw.getId(), List.of());
                    boolean isLiked = likedRawProductIds.contains(raw.getProductId());

                    FurnitureProductsInfoResponseV2.ProductInfo product = new FurnitureProductsInfoResponseV2.ProductInfo(
                            raw.getId(),
                            raw.getProductId(),
                            categoryName,
                            raw.getSource(),
                            raw.getBrand(),
                            raw.getProductName(),
                            raw.getProductImageUrl(),
                            raw.getListPrice(),
                            raw.getDiscountRate(),
                            raw.getDiscountPrice(),
                            raw.getProductMallName(),
                            raw.getProductSiteUrl(),
                            colors,
                            isLiked
                    );
                    return FurnitureProductsInfoResponseV2.ProductWrapper.of(product);
                })
                .toList();

        return FurnitureProductsInfoResponseV2.of(userName, products);
    }

    private Map<Long, List<ProductColorResponse>> buildColorMap(List<Long> rawProductIds) {
        List<CurationRawProductColor> colorEntities =
                curationRawProductColorRepository.findAllByCurationRawProductIdIn(rawProductIds);

        return colorEntities.stream()
                .filter(c -> c.getCurationRawProduct() != null && c.getCurationRawProduct().getId() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getCurationRawProduct().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .map(this::resolveColorName)
                                        .filter(Objects::nonNull)
                                        .distinct()
                                        .map(ProductColorResponse::fromName)
                                        .toList()
                        )
                ));
    }

    private Set<Long> findLikedRawProductIds(User user, List<CurationRawProduct> rawProducts) {
        if (user == null) {
            return Set.of();
        }

        List<Long> productIds = rawProducts.stream()
                .map(CurationRawProduct::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (productIds.isEmpty()) {
            return Set.of();
        }

        List<RecommendFurniture> recommendFurnitures =
                recommendFurnitureRepository.findAllBySourceAndFurnitureProductIdIn(CurationSource.RAW, productIds);

        if (recommendFurnitures.isEmpty()) {
            return Set.of();
        }

        List<Long> recommendIds = recommendFurnitures.stream()
                .map(RecommendFurniture::getId)
                .toList();

        return jjymRepository.findAllByUserIdAndRecommendFurnitureIdIn(user.getId(), recommendIds).stream()
                .map(Jjym::getRecommendFurniture)
                .filter(Objects::nonNull)
                .map(RecommendFurniture::getFurnitureProductId)
                .collect(Collectors.toSet());
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
}
