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
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductDetailResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureTypeFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.PriceRangeFilterResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.FurnitureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public CurationProductDetailResponse getProductDetail(Long id, User user) {
        CurationRawProduct product = curationRawProductRepository.findByIdAndIsExposedTrue(id)
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

        // curation_raw_products 데이터는 CurationSource.RAW로 관리됨
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
        // 리뷰 반영: 다중 매핑 시 우선순위(priority)가 가장 높은(숫자가 낮은) 태그를 대표 카테고리로 선택
        return product.getFurnitureTagMappings().stream()
                .map(CurationRawProductFurnitureTag::getFurnitureTag)
                .sorted(java.util.Comparator.comparing(FurnitureTag::getPriority))
                .findFirst()
                .map(tag -> {
                    Furniture furniture = tag.getFurniture();
                    FurnitureType type = furniture.getFurnitureType();

                    // 필터 API에서 정의한 사용자 친화적 레이블 매핑 로직 재사용
                    return mapToFriendlyLabel(type, furniture);
                })
                .orElse("기타"); // 매핑 정보가 없으면 기본값 '기타'
    }

    private String mapToFriendlyLabel(FurnitureType type, Furniture furniture) {
        String typeEng = type.getNameEng() != null ? type.getNameEng().toUpperCase().trim() : "";
        String furnitureEng = furniture.getFurnitureNameEng() != null ? furniture.getFurnitureNameEng().toUpperCase().trim() : "";

        // 1. 중분류(Furniture) 우선 매핑 (O(1) switch)
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

        // 2. 대분류(FurnitureType) 매핑 (O(1) switch)
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
        // DB에서 실시간 데이터 로드 (안정성 확보)
        List<FurnitureType> types = furnitureTypeRepository.findAll();
        List<Furniture> furnitures = furnitureRepository.findAll();

        return List.of(
                new FurnitureTypeFilterResponse(0L, "전체", "ALL"),
                findType(types, "BED", "침대/프레임"),
                findFurniture(furnitures, "OFFICE_DESK", "업무용 책상"),
                findFurniture(furnitures, "DINING_TABLE", "식탁"),
                findFurniture(furnitures, "SITTING_TABLE", "좌식 테이블"),
                findFurniture(furnitures, "CLOSET", "옷장"),
                findType(types, "STORAGE", "수납/장식장"),
                findType(types, "SOFA", "소파"),
                new FurnitureTypeFilterResponse(-1L, "의자/스툴", "CHAIR"),
                new FurnitureTypeFilterResponse(-1L, "화장대/협탁", "DRESSER"),
                new FurnitureTypeFilterResponse(-1L, "조명", "LIGHTING"),
                findType(types, "ETC", "기타")
        );
    }

    private FurnitureTypeFilterResponse findType(List<FurnitureType> types, String nameEng, String labelKr) {
        return types.stream()
                .filter(t -> t.getNameEng() != null && t.getNameEng().trim().equalsIgnoreCase(nameEng))
                .findFirst()
                .map(t -> new FurnitureTypeFilterResponse(t.getId(), labelKr, t.getNameEng().trim()))
                .orElse(new FurnitureTypeFilterResponse(-1L, labelKr, nameEng));
    }

    private FurnitureTypeFilterResponse findFurniture(List<Furniture> furnitures, String nameEng, String labelKr) {
        return furnitures.stream()
                .filter(f -> f.getFurnitureNameEng() != null && f.getFurnitureNameEng().trim().equalsIgnoreCase(nameEng))
                .findFirst()
                .map(f -> new FurnitureTypeFilterResponse(f.getId(), labelKr, f.getFurnitureNameEng().trim()))
                .orElse(new FurnitureTypeFilterResponse(-1L, labelKr, nameEng));
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

        long id = 1L;
        for (java.util.Map.Entry<String, String> entry : standardFilters.entrySet()) {
            responses.add(new ColorFilterResponse(id++, entry.getKey(), entry.getValue()));
        }

        return responses;
    }
}
