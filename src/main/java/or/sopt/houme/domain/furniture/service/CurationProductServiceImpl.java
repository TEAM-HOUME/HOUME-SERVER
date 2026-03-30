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
                findType(types, "SELECTIVE", "그 외")
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

        long id = 10L;
        for (java.util.Map.Entry<String, String> entry : standardFilters.entrySet()) {
            responses.add(new ColorFilterResponse(id++, entry.getKey(), entry.getValue()));
        }

        return responses;
    }
}
