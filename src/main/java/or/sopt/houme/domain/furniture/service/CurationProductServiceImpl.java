package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.presentation.dto.response.ColorFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductAppliedFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductDetailResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductMetaResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureTypeFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.PriceRangeFilterResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepositoryCustom;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.FurnitureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurationProductServiceImpl implements CurationProductService {

    private final FurnitureTypeRepository furnitureTypeRepository;
    private final FurnitureRepository furnitureRepository;
    private final CurationRawProductRepository curationRawProductRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;
    private final RecommendFurnitureRepository recommendFurnitureRepository;
    private final JjymRepository jjymRepository;

    @Override
    public CurationProductFilterResponse getFilterMetadata() {
        return new CurationProductFilterResponse(
                getFurnitureTypeFilters(),
                getPriceRangeFilters(),
                getColorFilters()
        );
    }

    @Override
    public CurationProductListResponse getProducts(
            String keyword,
            List<Long> typeIds,
            List<String> priceRangeIds,
            List<Long> colorIds,
            Long cursor,
            Integer size
    ) {
        validatePaginationParams(size);

        Pageable pageable = PageRequest.of(0, size);
        List<String> colorNames = extractColorNames(colorIds);
        List<CurationRawProductRepositoryCustom.PriceRangeFilter> priceFilters = extractPriceFilters(priceRangeIds);

        // Sentinel 값(0L, 음수) 전처리
        List<Long> filteredTypeIds = preprocessTypeIds(typeIds);
        
        // 0L(전체)이 포함되어 있다면 가구 유형 필터링을 적용하지 않음
        if (typeIds != null && typeIds.contains(0L)) {
            filteredTypeIds = null; 
        }

        Slice<CurationRawProduct> productSlice = curationRawProductRepository.findAllByCurationFilters(
                keyword, filteredTypeIds, priceFilters, colorNames, cursor, pageable
        );

        List<CurationProductResponse> products = productSlice.getContent().stream()
                .map(p -> new CurationProductResponse(
                        p.getId(),
                        p.getProductId(),
                        extractCategoryName(p),
                        p.getSource(),
                        p.getBrand(),
                        p.getProductName(),
                        p.getProductImageUrl(),
                        p.getListPrice(),
                        p.getDiscountRate(),
                        p.getDiscountPrice(),
                        p.getProductMallName(),
                        p.getProductSiteUrl()
                ))
                .toList();

        boolean hasNext = productSlice.hasNext();
        Long nextCursor = hasNext ? products.get(products.size() - 1).id() : null;
        List<CurationProductAppliedFilterResponse> appliedFilters = buildAppliedFilters(typeIds, priceRangeIds, colorIds);

        return new CurationProductListResponse(
                products,
                new CurationProductMetaResponse(nextCursor, hasNext, appliedFilters)
        );
    }

    private void validatePaginationParams(Integer size) {
        if (size == null || size <= 0 || size > 100) {
            throw new FurnitureException(ErrorCode.NOT_VALID_EXCEPTION);
        }
    }

    private List<Long> preprocessTypeIds(List<Long> typeIds) {
        if (typeIds == null || typeIds.isEmpty()) return null;
        
        // 실제 PK(양수)만 추출
        List<Long> actualIds = typeIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
        
        // 음수 ID(매핑불가 항목)만 선택했다면 절대로 매칭될 수 없는 값(-999L)을 넣어 빈 결과 유도
        boolean hasOnlyNegativeIds = actualIds.isEmpty() && typeIds.stream().anyMatch(id -> id < 0);
        return hasOnlyNegativeIds ? List.of(-999L) : (actualIds.isEmpty() ? null : actualIds);
    }

    private List<String> extractColorNames(List<Long> colorIds) {
        if (colorIds == null || colorIds.isEmpty()) return List.of();

        java.util.Map<String, String> standardColors = ColorHexMapper.getStandardColorFilters();
        java.util.List<String> allColorNames = new java.util.ArrayList<>(standardColors.keySet());

        return colorIds.stream()
                .filter(id -> id != null && id >= 1 && id <= allColorNames.size())
                .map(id -> allColorNames.get(id.intValue() - 1))
                .toList();
    }

    private List<CurationRawProductRepositoryCustom.PriceRangeFilter> extractPriceFilters(List<String> priceRangeIds) {
        if (priceRangeIds == null || priceRangeIds.isEmpty()) return List.of();
        if (priceRangeIds.stream().anyMatch(id -> "P0".equalsIgnoreCase(id))) return List.of();

        List<PriceRangeFilterResponse> allPriceMetadata = getPriceRangeFilters();
        return priceRangeIds.stream()
                .map(id -> allPriceMetadata.stream()
                        .filter(meta -> meta.id().equalsIgnoreCase(id))
                        .findFirst()
                        .map(meta -> new CurationRawProductRepositoryCustom.PriceRangeFilter(meta.min(), meta.max()))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<CurationProductAppliedFilterResponse> buildAppliedFilters(
            List<Long> typeIds, List<String> priceRangeIds, List<Long> colorIds
    ) {
        List<CurationProductAppliedFilterResponse> filters = new java.util.ArrayList<>();

        if (typeIds != null) {
            List<FurnitureTypeFilterResponse> allTypes = getFurnitureTypeFilters();
            for (Long id : typeIds) {
                allTypes.stream()
                        .filter(t -> t.id().equals(id))
                        .findFirst()
                        .ifPresent(t -> filters.add(new CurationProductAppliedFilterResponse("type", t.id().toString(), t.nameKr(), null)));
            }
        }

        if (priceRangeIds != null) {
            List<PriceRangeFilterResponse> allPrices = getPriceRangeFilters();
            for (String id : priceRangeIds) {
                allPrices.stream()
                        .filter(p -> p.id().equalsIgnoreCase(id))
                        .findFirst()
                        .ifPresent(p -> filters.add(new CurationProductAppliedFilterResponse("price", p.id(), p.label(), null)));
            }
        }

        if (colorIds != null) {
            java.util.Map<String, String> standardColors = ColorHexMapper.getStandardColorFilters();
            java.util.List<java.util.Map.Entry<String, String>> entries = new java.util.ArrayList<>(standardColors.entrySet());
            for (Long id : colorIds) {
                if (id != null && id >= 1 && id <= entries.size()) {
                    java.util.Map.Entry<String, String> entry = entries.get(id.intValue() - 1);
                    filters.add(new CurationProductAppliedFilterResponse("color", id.toString(), entry.getKey(), entry.getValue()));
                }
            }
        }

        return filters;
    }

    @Override
    public CurationProductListResponse getProductsV2(
            String keyword,
            List<Long> typeIds,
            List<String> priceRangeIds,
            List<Long> colorIds,
            Long cursor,
            Integer size
    ) {
        validatePaginationParams(size);

        Pageable pageable = PageRequest.of(0, size);
        List<String> colorNames = extractColorNames(colorIds);
        List<CurationRawProductRepositoryCustom.PriceRangeFilter> priceFilters = extractPriceFilters(priceRangeIds);

        List<Long> filteredTypeIds = preprocessTypeIds(typeIds);
        if (typeIds != null && typeIds.contains(0L)) {
            filteredTypeIds = null;
        }

        Slice<CurationRawProduct> productSlice = curationRawProductRepository.findAllByCurationFiltersV2(
                keyword, filteredTypeIds, priceFilters, colorNames, cursor, pageable
        );

        List<CurationProductResponse> products = productSlice.getContent().stream()
                .map(p -> new CurationProductResponse(
                        p.getId(),
                        p.getProductId(),
                        extractCategoryName(p),
                        p.getSource(),
                        p.getBrand(),
                        p.getProductName(),
                        p.getProductImageUrl(),
                        p.getListPrice(),
                        p.getDiscountRate(),
                        p.getDiscountPrice(),
                        p.getProductMallName(),
                        p.getProductSiteUrl()
                ))
                .toList();

        boolean hasNext = productSlice.hasNext();
        Long nextCursor = hasNext ? products.get(products.size() - 1).id() : null;
        List<CurationProductAppliedFilterResponse> appliedFilters = buildAppliedFilters(typeIds, priceRangeIds, colorIds);

        return new CurationProductListResponse(
                products,
                new CurationProductMetaResponse(nextCursor, hasNext, appliedFilters)
        );
    }

    @Override
    public CurationProductDetailResponse getProductDetail(Long id, User user) {
        CurationRawProduct product = curationRawProductRepository.findByIdAndIsExposedTrueOrNull(id)
                .orElseThrow(() -> new FurnitureException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT));

        String categoryName = extractCategoryName(product);
        List<CurationProductDetailResponse.ProductColorDetail> colors = extractColorDetails(id);
        boolean isLiked = checkIsLiked(product, user);

        return new CurationProductDetailResponse(
                new CurationProductDetailResponse.ProductDetail(
                        product.getId(),
                        product.getProductId(),
                        categoryName,
                        product.getSource(),
                        product.getBrand(),
                        product.getProductName(),
                        product.getProductImageUrl(),
                        product.getListPrice(),
                        product.getDiscountRate(),
                        product.getDiscountPrice(),
                        product.getProductMallName(),
                        product.getProductSiteUrl(),
                        colors,
                        isLiked
                )
        );
    }

    private boolean checkIsLiked(CurationRawProduct product, User user) {
        if (user == null) {
            return false;
        }
        return recommendFurnitureRepository.findBySourceAndFurnitureProductId(CurationSource.RAW, product.getProductId())
                .map(recommend -> jjymRepository.existsByUserIdAndRecommendFurnitureId(user.getId(), recommend.getId()))
                .orElse(false);
    }

    private List<CurationProductDetailResponse.ProductColorDetail> extractColorDetails(Long productId) {
        List<CurationRawProductColor> colorEntities = curationRawProductColorRepository.findAllByCurationRawProductId(productId);
        List<CurationProductDetailResponse.ProductColorDetail> details = new java.util.ArrayList<>();

        for (CurationRawProductColor colorEntity : colorEntities) {
            String clientColorName = colorEntity.getClientColorName();
            if (clientColorName == null || clientColorName.isBlank()) {
                continue;
            }

            String[] names = clientColorName.split("[,/]");
            for (String name : names) {
                String trimmedName = name.trim();
                if (trimmedName.isEmpty()) continue;

                List<String> hexCodes = ColorHexMapper.toHexCodes(List.of(trimmedName));
                String hexValue = hexCodes.isEmpty() ? null : hexCodes.get(0);

                if (hexValue != null && !hexValue.startsWith("#")) {
                    hexValue = null;
                }

                details.add(new CurationProductDetailResponse.ProductColorDetail(trimmedName, hexValue));
            }
        }
        return details;
    }

    private String extractCategoryName(CurationRawProduct product) {
        return product.getFurnitureTagMappings().stream()
                .map(CurationRawProductFurnitureTag::getFurnitureTag)
                .sorted(java.util.Comparator.comparing(FurnitureTag::getPriority))
                .filter(tag -> tag.getFurniture() != null && tag.getFurniture().getFurnitureType() != null)
                .findFirst()
                .map(tag -> {
                    Furniture furniture = tag.getFurniture();
                    FurnitureType type = furniture.getFurnitureType();
                    return mapToFriendlyLabel(type, furniture);
                })
                .orElse("기타");
    }

    private String mapToFriendlyLabel(FurnitureType type, Furniture furniture) {
        String typeEng = type.getNameEng() != null ? type.getNameEng().toUpperCase().trim() : "";
        String furnitureEng = furniture.getFurnitureNameEng() != null ? furniture.getFurnitureNameEng().toUpperCase().trim() : "";

        switch (furnitureEng) {
            case "OFFICE_DESK": return "업무용 책상";
            case "DINING_TABLE": return "식탁";
            case "SITTING_TABLE": return "좌식 테이블";
            case "CLOSET": return "옷장";
            case "SINGLE_SOFA":
            case "CHAIR": return "의자/스툴";
            case "DRESSER": return "화장대/협탁";
            default: break;
        }

        return switch (typeEng) {
            case "BED" -> "침대/프레임";
            case "STORAGE" -> "수납/장식장";
            case "SOFA" -> "소파";
            case "LIGHTING" -> "조명";
            case "SELECTIVE" -> "그 외";
            case "ETC" -> "기타";
            default -> "기타";
        };
    }

    private List<FurnitureTypeFilterResponse> getFurnitureTypeFilters() {
        List<FurnitureType> types = furnitureTypeRepository.findAll();
        List<Furniture> furnitures = furnitureRepository.findAll();

        return List.of(
                new FurnitureTypeFilterResponse(0L, "전체", "ALL"),
                findType(types, "BED", "침대/프레임", -10L),
                findFurniture(furnitures, "OFFICE_DESK", "업무용 책상", -11L),
                findFurniture(furnitures, "DINING_TABLE", "식탁", -12L),
                findFurniture(furnitures, "SITTING_TABLE", "좌식 테이블", -13L),
                findFurniture(furnitures, "CLOSET", "옷장", -14L),
                findType(types, "STORAGE", "수납/장식장", -15L),
                findType(types, "SOFA", "소파", -16L),
                new FurnitureTypeFilterResponse(-1L, "의자/스툴", "CHAIR"),
                new FurnitureTypeFilterResponse(-2L, "화장대/협탁", "DRESSER"),
                new FurnitureTypeFilterResponse(-3L, "조명", "LIGHTING"),
                findType(types, "ETC", "기타", -17L)
        );
    }

    private FurnitureTypeFilterResponse findType(List<FurnitureType> types, String nameEng, String labelKr, Long fallbackId) {
        return types.stream()
                .filter(t -> t.getNameEng() != null && t.getNameEng().trim().equalsIgnoreCase(nameEng))
                .findFirst()
                .map(t -> new FurnitureTypeFilterResponse(t.getId(), labelKr, t.getNameEng().trim()))
                .orElse(new FurnitureTypeFilterResponse(fallbackId, labelKr, nameEng));
    }

    private FurnitureTypeFilterResponse findFurniture(List<Furniture> furnitures, String nameEng, String labelKr, Long fallbackId) {
        return furnitures.stream()
                .filter(f -> f.getFurnitureNameEng() != null && f.getFurnitureNameEng().trim().equalsIgnoreCase(nameEng))
                .findFirst()
                .map(f -> new FurnitureTypeFilterResponse(f.getId(), labelKr, f.getFurnitureNameEng().trim()))
                .orElse(new FurnitureTypeFilterResponse(fallbackId, labelKr, nameEng));
    }


    private List<PriceRangeFilterResponse> getPriceRangeFilters() {
        return List.of(
                new PriceRangeFilterResponse("P0", "전체", null, null),
                new PriceRangeFilterResponse("P1", "5만원 이하", 0L, 50000L),
                new PriceRangeFilterResponse("P2", "5-10만원", 50001L, 100000L),
                new PriceRangeFilterResponse("P3", "10만원대", 100001L, 200000L),
                new PriceRangeFilterResponse("P4", "20만원대", 200001L, 300000L),
                new PriceRangeFilterResponse("P5", "30만원대", 300001L, 400000L),
                new PriceRangeFilterResponse("P6", "40만원대", 400001L, 500000L),
                new PriceRangeFilterResponse("P7", "50만원 이상", 500001L, null)
        );
    }

    private List<ColorFilterResponse> getColorFilters() {
        java.util.Map<String, String> standardFilters = ColorHexMapper.getStandardColorFilters();
        java.util.List<ColorFilterResponse> responses = new java.util.ArrayList<>();
        responses.add(new ColorFilterResponse(0L, "전체", null));

        long id = 1L;
        for (java.util.Map.Entry<String, String> entry : standardFilters.entrySet()) {
            responses.add(new ColorFilterResponse(id++, entry.getKey(), entry.getValue()));
        }

        return responses;
    }
}
