package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.dto.ActivityItem;
import or.sopt.houme.domain.furniture.dto.FurnitureGroup;
import or.sopt.houme.domain.furniture.dto.FurnitureItem;
import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.entity.FurnitureType;
import or.sopt.houme.domain.furniture.entity.FurnitureTypes;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.house.entity.enums.Activity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FurnitureServiceImpl implements FurnitureService {

    private final FurnitureRepository furnitureRepository;

    // 가구 반환
    @Override
    public FurnitureAndActivityResponse getFurnitureAndActivity() {

        // 침대 가구들
        List<FurnitureItem> bedItems = new ArrayList<>();
        // 선택 가구들
        List<FurnitureItem> selectiveItems = new ArrayList<>();

        // 가구 타입별 구분
        List<Furniture> furnitureList = Optional.of(furnitureRepository.findAll())
                .orElse(Collections.emptyList());

        for (Furniture item : furnitureList) {
            FurnitureType type = item.getFurnitureType();
            if (type == null || type.getFurnitureType() == null) continue;

            FurnitureItem furnitureItem = FurnitureItem.from(item);
            if (type.getFurnitureType() == FurnitureTypes.BED) {
                bedItems.add(furnitureItem);
            } else {
                selectiveItems.add(furnitureItem);
            }
        }

        // 필수 값 (BED)
        FurnitureGroup bedGroup = FurnitureGroup.from(true, bedItems);
        // 필수 아님 (그 외 가구)
        FurnitureGroup selectiveGroup = FurnitureGroup.from(false, selectiveItems);

        // 주요 활동 담기
        List<ActivityItem> activities = Arrays.stream(Activity.values())
                .map(ActivityItem::from)
                .toList();

        // 반환 Response 생성
        return FurnitureAndActivityResponse.of(activities, bedGroup, selectiveGroup);
    }
}
