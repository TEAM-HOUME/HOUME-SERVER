package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.infrastructure.client.FastApiImageHashClient;
import or.sopt.houme.domain.furniture.infrastructure.client.NaverShopApiClient;
import or.sopt.houme.domain.furniture.presentation.dto.ActivityItem;
import or.sopt.houme.domain.furniture.presentation.dto.FurnitureItem;
import or.sopt.houme.domain.furniture.presentation.dto.response.ActivityWithFurnitureResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureCategoryItem;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureCategoryGroup;
import or.sopt.houme.domain.furniture.model.entity.ActivityFurniture;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.repository.ActivityFurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.repository.taste.tag.TagRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.HouseException;
import or.sopt.houme.global.api.handler.TagException;
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
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final HouseRepository houseRepository;
    private final FurnitureTagRepository furnitureTagRepository;
    private final FurnitureTypeRepository furnitureTypeRepository;
    private final ActivityFurnitureRepository activityFurnitureRepository;

    private final NaverShopApiClient naverShopApiClient;
    private final FastApiImageHashClient imageHashClient;

    // 가구 반환
    @Cacheable(value = "furnitureAndActivityCache")
    @Override
    public FurnitureAndActivityResponse getFurnitureAndActivity() {
        List<FurnitureCategoryGroup> list = getDashboardCategories();

        // 주요 활동 담기
        List<ActivityItem> activities = Arrays.stream(Activity.values())
                .map(ActivityItem::from)
                .toList();

        // 반환 Response 생성
        return FurnitureAndActivityResponse.of(activities, list);
    }

    @Override
    public List<FurnitureCategoryGroup> getDashboardCategories() {
        // 모든 카테고리 가져오기
        List<FurnitureType> furnitureTypes = furnitureTypeRepository.findAll();

        // findAllWithFurnitureType() 으로 N+1 방지
        List<Furniture> furnitureList = furnitureRepository.findAllWithFurnitureType();

        // FurnitureType 별로 그룹화
        Map<Long, List<FurnitureItem>> furnitureByCategory = furnitureList.stream()
                .collect(Collectors.groupingBy(
                        furniture -> furniture.getFurnitureType().getId(),  // FurnitureType Id 가져오기
                        Collectors.collectingAndThen(
                                Collectors.mapping(FurnitureItem::from, Collectors.toList()),
                                list -> {
                                    list.sort(
                                            Comparator.comparing(
                                                            FurnitureItem::priority,
                                                            Comparator.nullsLast(Comparator.naturalOrder())
                                                    )
                                                    .thenComparing(FurnitureItem::id, Comparator.nullsLast(Comparator.naturalOrder()))
                                    );
                                    return list;
                                }
                        )
                ));

        // 각 FurnitureType에 해당하는 FurnitureGroup 생성
        return furnitureTypes.stream()
                .sorted(
                        Comparator.comparing(
                                        FurnitureType::getPriority,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                                .thenComparing(FurnitureType::getId, Comparator.nullsLast(Comparator.naturalOrder()))
                )
                .map(furnitureType -> {
                    // 없으면 빈 리스트
                    List<FurnitureCategoryItem> items = furnitureByCategory
                            .getOrDefault(furnitureType.getId(), Collections.emptyList())
                            .stream()
                            .map(FurnitureCategoryItem::from)
                            .toList();
                    return FurnitureCategoryGroup.from(furnitureType, items);
                })
                .toList();
    }

    @Override
    public List<ActivityWithFurnitureResponse> getActivityFurnitureMappings() {
        List<ActivityFurniture> mappings = activityFurnitureRepository.findAllByOrderByPriorityAscIdAsc();
        Map<Activity, List<FurnitureItem>> grouped = new LinkedHashMap<>();

        for (ActivityFurniture mapping : mappings) {
            grouped.computeIfAbsent(mapping.getActivity(), key -> new ArrayList<>())
                    .add(FurnitureItem.from(mapping.getFurniture(), mapping.getPriority()));
        }

        return grouped.entrySet().stream()
                .map(entry -> ActivityWithFurnitureResponse.of(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }

    @Override
    public FurnitureCategoriesResponse getFurnitureCategoriesByStyle(User user, Long imageId, List<String> detectedObjects) {

        // 1. userId와 imageId로 해당하는 스타일 태그 조회
        Tag tag = findTag(user, imageId);

        // 2. userId와 imageId로 이미지 생성시 선택했던 가구들을 조회
        List<Furniture> selectedFurnitures = findSelectedFurnitures(user, imageId);

        // 3. alias map 정의, 침대의 키워드의 확장
        Set<String> expandedRequestedObjects = expandKeywords(detectedObjects);

        // 4. selectedFurnitures와 expandedRequestedObjects의 교집합 산출
        List<Furniture> intersectedFurnitures = filterIntersectedFurnitures(selectedFurnitures, expandedRequestedObjects);

        // 5. 교집합으로 산출된 가구들과 스타일 태그에 해당하는 매핑 객체를 furniture_tags에서 조회
        List<FurnitureTag> matchedTags = furnitureTagRepository.findAllByTagIdAndFurnitureIn(tag.getId(), intersectedFurnitures);

        // 6. priority 기준 오름차순 정렬 → 응답 DTO 변환
        List<FurnitureCategoriesResponse.FurnitureCategoryResponse> categoryResponses = matchedTags.stream()
                .sorted(Comparator.comparingInt(FurnitureTag::getPriority))
                .map(ft -> FurnitureCategoriesResponse.FurnitureCategoryResponse.of(
                        ft.getFurniture().getId(),
                        ft.getFurniture().getFurnitureNameKr()
                ))
                .toList();

        return FurnitureCategoriesResponse.of(categoryResponses);
    }

    @Override
    public Optional<Long> findBedId(List<Long> furnitureIds) {
        String BED = "BED";

        return furnitureRepository.findAllById(furnitureIds)
                .stream()
                .filter(furniture -> BED.equals(furniture.getFurnitureType().getNameEng()))
                .map(Furniture::getId)
                .findFirst();
    }

    @Override
    public FurnitureTag findFurnitureTag(User user, Long imageId, Long categoryId) {

        // 1. userId와 imageId로 스타일 태그 조회
        Tag tag = tagRepository.findTagByUserIdAndImageId(user.getId(), imageId)
                .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        // 2. categoryId로 furniture 객체 조회
        Furniture furniture = furnitureRepository.findById(categoryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        // 3. tagId와 categoryId(=furnitureId)로 furnitureTag 매핑 객체 조회
        return furnitureTagRepository.findByFurnitureAndTag(furniture, tag)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));
    }

    // 기획의사결정용
    @Override
    public FurnitureTag findFurnitureTagForPlan(Long tagId, Long furnitureId) {
        // 1. tagId로 스타일 태그 조회
        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        // 2. categoryId로 furniture 객체 조회
        Furniture furniture = furnitureRepository.findById(furnitureId).orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        // 3. tagId와 categoryId(=furnitureId)로 furnitureTag 매핑 객체 조회
        return furnitureTagRepository.findByFurnitureAndTag(furniture, tag).orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));
    }

    /**
     * 보조 메서드 (비즈니스 로직 가독성을 위해 분리했습니다.)
     */
    private Tag findTag(User user, Long imageId) {
        return tagRepository.findTagByUserIdAndImageId(user.getId(), imageId)
                .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));
    }

    private List<Furniture> findSelectedFurnitures(User user, Long imageId) {
        House house = houseRepository.findHouseByUserIdAndImageId(user.getId(), imageId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE_ENTITY));
        return furnitureRepository.findAllByHouseId(house.getId());
    }

    // single 침대 키워드를 현재는 구분하지 않고 있기 때문에, 다른 침대 종류와도 확장 매핑합니다.
    private Set<String> expandKeywords(List<String> detectedObjects) {
        Set<String> requestedObjects = detectedObjects.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Map<String, List<String>> keywordAliasMap = Map.of(
                "single", List.of("single", "super_single", "double", "queen_over")
        );

        return requestedObjects.stream()
                .flatMap(req -> keywordAliasMap.getOrDefault(req, List.of(req)).stream())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private List<Furniture> filterIntersectedFurnitures(List<Furniture> furnitures, Set<String> keywords) {
        return furnitures.stream()
                .filter(f -> f.getFurnitureNameEng() != null
                        && keywords.contains(f.getFurnitureNameEng().toLowerCase()))
                .toList();
    }
}
