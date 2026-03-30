package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.presentation.dto.response.ColorFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureTypeFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.PriceRangeFilterResponse;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductDetailResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
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

    @Override
    public CurationProductFilterResponse getFilterMetadata() {
        return new CurationProductFilterResponse(
                getFurnitureTypeFilters(),
                getPriceRangeFilters(),
                getColorFilters()
        );
    }

    @Override
    public CurationProductDetailResponse getProductDetail(Long id) {
        CurationRawProduct product = curationRawProductRepository.findById(id)
                .orElseThrow(() -> new FurnitureException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT));

        String categoryName = extractCategoryName(product);

        return new CurationProductDetailResponse(
                new CurationProductDetailResponse.ProductDetail(
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
                        product.getProductSiteUrl()
                )
        );
    }

    private String extractCategoryName(CurationRawProduct product) {
        // 상품에 매핑된 첫 번째 가구 태그 정보를 바탕으로 카테고리명 추출
        return product.getFurnitureTagMappings().stream()
                .findFirst()
                .map(mapping -> {
                    Furniture furniture = mapping.getFurnitureTag().getFurniture();
                    FurnitureType type = furniture.getFurnitureType();

                    // 필터 API에서 정의한 사용자 친화적 레이블 매핑 로직 재사용
                    return mapToFriendlyLabel(type, furniture);
                })
                .orElse("기타"); // 매핑 정보가 없으면 기본값 '기타'
    }

    private String mapToFriendlyLabel(FurnitureType type, Furniture furniture) {
        String typeEng = type.getNameEng();
        String furnitureEng = furniture.getFurnitureNameEng();

        // 1. 중분류(Furniture) 우선 매핑
        if ("OFFICE_DESK".equalsIgnoreCase(furnitureEng)) return "업무용 책상";
        if ("DINING_TABLE".equalsIgnoreCase(furnitureEng)) return "식탁";
        if ("SITTING_TABLE".equalsIgnoreCase(furnitureEng)) return "좌식 테이블";
        if ("CLOSET".equalsIgnoreCase(furnitureEng)) return "옷장";
        if ("SINGLE_SOFA".equalsIgnoreCase(furnitureEng)) return "의자/스툴";
        if ("CHAIR".equalsIgnoreCase(furnitureEng)) return "의자/스툴";
        if ("DRESSER".equalsIgnoreCase(furnitureEng)) return "화장대/협탁";

        // 2. 대분류(FurnitureType) 매핑
        if ("BED".equalsIgnoreCase(typeEng)) return "침대/프레임";
        if ("STORAGE".equalsIgnoreCase(typeEng)) return "수납/장식장";
        if ("SOFA".equalsIgnoreCase(typeEng)) return "소파";
        if ("LIGHTING".equalsIgnoreCase(typeEng)) return "조명";
        if ("SELECTIVE".equalsIgnoreCase(typeEng)) return "그 외";
        if ("ETC".equalsIgnoreCase(typeEng)) return "기타";

        return "기타";
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
