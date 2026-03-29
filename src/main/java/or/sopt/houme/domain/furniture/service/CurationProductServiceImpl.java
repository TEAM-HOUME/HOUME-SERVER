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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurationProductServiceImpl implements CurationProductService {

    private final FurnitureTypeRepository furnitureTypeRepository;
    private final FurnitureRepository furnitureRepository;

    @Override
    public CurationProductFilterResponse getFilterMetadata() {
        return new CurationProductFilterResponse(
                getFurnitureTypeFilters(),
                getPriceRangeFilters(),
                getColorFilters()
        );
    }

    private List<FurnitureTypeFilterResponse> getFurnitureTypeFilters() {
        // DB에서 실시간 데이터 로드 (안정성 확보)
        List<FurnitureType> types = furnitureTypeRepository.findAll();
        List<Furniture> furnitures = furnitureRepository.findAll();

        return List.of(
                new FurnitureTypeFilterResponse(0L, "전체", "ALL"),
                findType(types, "BED", "침대/프레임"),          // 대분류
                findFurniture(furnitures, "OFFICE_DESK", "업무용 책상"), // 중분류
                findFurniture(furnitures, "DINING_TABLE", "식탁"),     // 중분류
                findFurniture(furnitures, "SITTING_TABLE", "좌식 테이블"), // 중분류
                findFurniture(furnitures, "CLOSET", "옷장"),           // 중분류
                findType(types, "STORAGE", "수납/장식장"),      // 대분류
                findType(types, "SOFA", "소파"),                // 대분류
                findFurniture(furnitures, "CHAIR", "의자/스툴"),       // 중분류 (임시 매핑 제거)
                findFurniture(furnitures, "DRESSER", "화장대/협탁"),     // 중분류 (임시 매핑 제거)
                findType(types, "LIGHTING", "조명"),            // 대분류
                findType(types, "SELECTIVE", "그 외")           // 대분류
        );
    }

    private FurnitureTypeFilterResponse findType(List<FurnitureType> types, String nameEng, String labelKr) {
        return types.stream()
                .filter(t -> t.getNameEng() != null && t.getNameEng().trim().toUpperCase().contains(nameEng.toUpperCase()))
                .findFirst()
                .map(t -> new FurnitureTypeFilterResponse(t.getId(), labelKr, t.getNameEng().trim()))
                .orElse(new FurnitureTypeFilterResponse(-1L, labelKr, nameEng));
    }

    private FurnitureTypeFilterResponse findFurniture(List<Furniture> furnitures, String nameEng, String labelKr) {
        return furnitures.stream()
                .filter(f -> f.getFurnitureNameEng() != null && f.getFurnitureNameEng().trim().toUpperCase().contains(nameEng.toUpperCase()))
                .findFirst()
                .map(f -> new FurnitureTypeFilterResponse(f.getId(), labelKr, f.getFurnitureNameEng().trim()))
                .orElse(new FurnitureTypeFilterResponse(-1L, labelKr, nameEng));
    }


    private List<PriceRangeFilterResponse> getPriceRangeFilters() {
        return List.of(
                new PriceRangeFilterResponse("P0", "전체", null, null),
                new PriceRangeFilterResponse("P1", "5만원 이하", 0L, 50000L),
                new PriceRangeFilterResponse("P2", "5-10만원", 50000L, 100000L),
                new PriceRangeFilterResponse("P3", "10만원대", 100000L, 200000L),
                new PriceRangeFilterResponse("P4", "20만원대", 200000L, 300000L),
                new PriceRangeFilterResponse("P5", "30만원대", 300000L, 400000L),
                new PriceRangeFilterResponse("P6", "40만원대", 400000L, 500000L),
                new PriceRangeFilterResponse("P7", "50만원 이상", 500000L, null)
        );
    }

    private List<ColorFilterResponse> getColorFilters() {
        return List.of(
                new ColorFilterResponse(10L, "블랙", "#000000"),
                new ColorFilterResponse(11L, "화이트", "#FFFFFF"),
                new ColorFilterResponse(12L, "브라운", "#8B4513"),
                new ColorFilterResponse(13L, "베이지", "#F5F5DC"),
                new ColorFilterResponse(14L, "그레이", "#808080"),
                new ColorFilterResponse(15L, "실버", "#C0C0C0"),
                new ColorFilterResponse(16L, "옐로우", "#FFFF00"),
                new ColorFilterResponse(17L, "블루", "#0000FF"),
                new ColorFilterResponse(18L, "그린", "#008000")
        );
    }
}
