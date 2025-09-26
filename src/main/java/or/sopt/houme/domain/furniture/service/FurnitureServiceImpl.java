package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.dto.ActivityItem;
import or.sopt.houme.domain.furniture.dto.FurnitureGroup;
import or.sopt.houme.domain.furniture.dto.FurnitureItem;
import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureCategoryGroup;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.entity.FurnitureType;
import or.sopt.houme.domain.furniture.entity.FurnitureTypes;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.house.entity.enums.Activity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FurnitureServiceImpl implements FurnitureService {

    private final FurnitureRepository furnitureRepository;
    private final FurnitureTypeRepository furnitureTypeRepository;

    // 가구 반환
    @Cacheable(value = "furnitureAndActivityCache")
    @Override
    public FurnitureAndActivityResponse getFurnitureAndActivity() {

        // 모든 카테고리 가져오기
        List<FurnitureType> furnitureTypes = furnitureTypeRepository.findAll();

        // 모든 가구 조회 (없는 경우는 빈 리스트)
        List<Furniture> furnitureList = Optional.of(furnitureRepository.findAll())
                .orElse(Collections.emptyList());

        // FurnitureType 별로 그룹화
        Map<Long, List<FurnitureItem>> furnitureByCategory = furnitureList.stream()
                .collect(Collectors.groupingBy(
                        furniture -> furniture.getFurnitureType().getId(),  // FurnitureType Id 가져오기
                        Collectors.mapping(FurnitureItem::from, Collectors.toList())
                ));

        // 각 FurnitureType에 해당하는 FurnitureGroup 생성
        List<FurnitureCategoryGroup> list = furnitureTypes.stream()
                .map(furnitureType -> {
                    // 없으면 빈 리스트
                    List<FurnitureItem> items = furnitureByCategory.getOrDefault(furnitureType.getId(), Collections.emptyList());
                    return FurnitureCategoryGroup.from(furnitureType, items);
                })
                .sorted(Comparator.comparing(FurnitureCategoryGroup::categoryId)) // 카테고리 ID로 정렬
                .toList();

        // 주요 활동 담기
        List<ActivityItem> activities = Arrays.stream(Activity.values())
                .map(ActivityItem::from)
                .toList();

        // 반환 Response 생성
        return FurnitureAndActivityResponse.of(activities, list);
    }
}
