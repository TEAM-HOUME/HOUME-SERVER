package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.presentation.dto.ActivityItem;
import or.sopt.houme.domain.furniture.presentation.dto.FurnitureItem;
import or.sopt.houme.domain.furniture.presentation.dto.response.ActivityWithFurnitureResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureCategoryItem;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureCategoryGroup;
import or.sopt.houme.furniture.domain.ActivityFurnitureView;
import or.sopt.houme.furniture.domain.Furniture;
import or.sopt.houme.furniture.domain.FurnitureTagView;
import or.sopt.houme.furniture.domain.FurnitureTypeView;
import or.sopt.houme.furniture.domain.FurnitureWithTypeView;
import or.sopt.houme.furniture.domain.port.out.ActivityFurnitureQueryPort;
import or.sopt.houme.furniture.domain.port.out.FurnitureRepositoryPort;
import or.sopt.houme.furniture.domain.port.out.FurnitureTagQueryPort;
import or.sopt.houme.furniture.domain.port.out.FurnitureTypeQueryPort;
import or.sopt.houme.house.domain.port.out.HouseQueryPort;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.tag.domain.Tag;
import or.sopt.houme.tag.domain.port.out.TagRepositoryPort;
import or.sopt.houme.user.domain.User;
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

    private static final Set<String> FUNNEL_EXCLUDED_TYPE_NAMEENGS = Set.of("ETC");
    private static final Set<String> FUNNEL_EXCLUDED_FURNITURE_NAMEENGS = Set.of(
            "CHAIR", "DRESSING_TABLE", "LIGHTING", "CLOSET", "ETC"
    );

    private final FurnitureRepositoryPort furnitureRepositoryPort;
    private final TagRepositoryPort tagRepositoryPort;
    private final HouseQueryPort houseQueryPort;
    private final FurnitureTagQueryPort furnitureTagQueryPort;
    private final FurnitureTypeQueryPort furnitureTypeQueryPort;
    private final ActivityFurnitureQueryPort activityFurnitureQueryPort;

    // [pbem22, 2026-05-28, #541] CurationRawProductFurniture 경로 폴백을 위해 추가
    private final CurationRawProductFurnitureService curationRawProductFurnitureService;

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
        List<FurnitureTypeView> furnitureTypes = furnitureTypeQueryPort.findAll().stream()
                .filter(t -> t.nameEng() == null || !FUNNEL_EXCLUDED_TYPE_NAMEENGS.contains(t.nameEng().toUpperCase()))
                .toList();

        List<FurnitureWithTypeView> furnitureList = furnitureRepositoryPort.findAllWithType().stream()
                .filter(f -> f.furnitureNameEng() == null || !FUNNEL_EXCLUDED_FURNITURE_NAMEENGS.contains(f.furnitureNameEng().toUpperCase()))
                .toList();

        // FurnitureType 별로 그룹화
        Map<Long, List<FurnitureItem>> furnitureByCategory = furnitureList.stream()
                .collect(Collectors.groupingBy(
                        FurnitureWithTypeView::furnitureTypeId,  // FurnitureType Id 가져오기
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
                                        FurnitureTypeView::priority,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                                .thenComparing(FurnitureTypeView::id, Comparator.nullsLast(Comparator.naturalOrder()))
                )
                .map(furnitureType -> {
                    // 없으면 빈 리스트
                    List<FurnitureCategoryItem> items = furnitureByCategory
                            .getOrDefault(furnitureType.id(), Collections.emptyList())
                            .stream()
                            .map(FurnitureCategoryItem::from)
                            .toList();
                    return FurnitureCategoryGroup.from(furnitureType, items);
                })
                .toList();
    }

    @Override
    public List<ActivityWithFurnitureResponse> getActivityFurnitureMappings() {
        List<ActivityFurnitureView> mappings = activityFurnitureQueryPort.findAllOrderByPriorityAscIdAsc();
        Map<Activity, List<FurnitureItem>> grouped = new LinkedHashMap<>();

        for (ActivityFurnitureView mapping : mappings) {
            grouped.computeIfAbsent(mapping.activity(), key -> new ArrayList<>())
                    .add(FurnitureItem.from(mapping.furniture(), mapping.priority()));
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
        List<FurnitureTagView> matchedTags = furnitureTagQueryPort.findAllByTagIdAndFurnitureIdIn(
                tag.getId(), intersectedFurnitures.stream().map(Furniture::getId).toList());
        return buildCategoryResponse(matchedTags);
    }

    @Override
    public FurnitureCategoriesResponse getFurnitureCategoriesByStyleV2(User user, Long imageId) {

        // 1. userId와 imageId로 해당하는 스타일 태그 조회
        Tag tag = findTag(user, imageId);

        // 2. userId와 imageId로 이미지 생성시 선택했던 가구들을 조회
        List<Furniture> selectedFurnitures = findSelectedFurnitures(user, imageId);

        // 3. 선택 가구들과 스타일 태그에 해당하는 매핑 객체를 furniture_tags에서 조회
        List<Long> selectedFurnitureIds = selectedFurnitures.stream().map(Furniture::getId).toList();
        List<FurnitureTagView> matchedTags = furnitureTagQueryPort.findAllByTagIdAndFurnitureIdIn(tag.getId(), selectedFurnitureIds);

        // [pbem22, 2026-05-28, #541] FurnitureTag 경로에 없는 가구도 CurationRawProductFurniture로 매핑된 경우 카테고리에 포함
        Set<Long> taggedFurnitureIds = matchedTags.stream()
                .map(FurnitureTagView::furnitureId)
                .collect(Collectors.toSet());
        List<Long> extraFurnitureIds = curationRawProductFurnitureService
                .getFurnitureIdsHavingProducts(selectedFurnitureIds).stream()
                .filter(id -> !taggedFurnitureIds.contains(id))
                .toList();
        List<Furniture> extraFurnitures = extraFurnitureIds.isEmpty()
                ? List.of()
                : furnitureRepositoryPort.findAllById(extraFurnitureIds);

        return buildCategoryResponseWithExtra(matchedTags, extraFurnitures);
    }

    @Override
    public Optional<Long> findBedId(List<Long> furnitureIds) {
        String BED = "BED";

        return furnitureRepositoryPort.findAllWithTypeByIdIn(furnitureIds)
                .stream()
                .filter(furniture -> BED.equals(furniture.furnitureTypeNameEng()))
                .map(FurnitureWithTypeView::id)
                .findFirst();
    }

    @Override
    public FurnitureTagView findFurnitureTag(User user, Long imageId, Long categoryId) {

        // 1. userId와 imageId로 스타일 태그 조회
        Tag tag = tagRepositoryPort.findTagByUserIdAndImageId(user.getId(), imageId)
                .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        // 2. categoryId로 furniture 객체 조회
        Furniture furniture = furnitureRepositoryPort.findById(categoryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        // 3. tagId와 categoryId(=furnitureId)로 furnitureTag 매핑 객체 조회
        return furnitureTagQueryPort.findByFurnitureIdAndTagId(furniture.getId(), tag.getId())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));
    }

    // 기획의사결정용
    @Override
    public FurnitureTagView findFurnitureTagForPlan(Long tagId, Long furnitureId) {
        // 1. tagId로 스타일 태그 조회
        Tag tag = tagRepositoryPort.findById(tagId).orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        // 2. categoryId로 furniture 객체 조회
        Furniture furniture = furnitureRepositoryPort.findById(furnitureId).orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        // 3. tagId와 categoryId(=furnitureId)로 furnitureTag 매핑 객체 조회
        return furnitureTagQueryPort.findByFurnitureIdAndTagId(furniture.getId(), tag.getId())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));
    }

    /**
     * 보조 메서드 (비즈니스 로직 가독성을 위해 분리했습니다.)
     */
    private Tag findTag(User user, Long imageId) {
        return tagRepositoryPort.findTagByUserIdAndImageId(user.getId(), imageId)
                .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));
    }

    private List<Furniture> findSelectedFurnitures(User user, Long imageId) {
        Long houseId = houseQueryPort.findHouseIdByUserIdAndImageId(user.getId(), imageId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE_ENTITY));
        return furnitureRepositoryPort.findAllByHouseId(houseId);
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

    private FurnitureCategoriesResponse buildCategoryResponse(List<FurnitureTagView> matchedTags) {
        List<FurnitureCategoriesResponse.FurnitureCategoryResponse> categoryResponses = matchedTags.stream()
                .sorted(Comparator.comparingInt(FurnitureTagView::priority))
                .map(ft -> FurnitureCategoriesResponse.FurnitureCategoryResponse.of(
                        ft.furnitureId(),
                        ft.furnitureNameKr()
                ))
                .toList();

        return FurnitureCategoriesResponse.of(categoryResponses);
    }

    // [pbem22, 2026-05-28, #541] FurnitureTag 경로 카테고리 + CurationRawProductFurniture 경로 카테고리 합산 반환
    private FurnitureCategoriesResponse buildCategoryResponseWithExtra(
            List<FurnitureTagView> matchedTags,
            List<Furniture> extraFurnitures
    ) {
        List<FurnitureCategoriesResponse.FurnitureCategoryResponse> fromTags = matchedTags.stream()
                .sorted(Comparator.comparingInt(FurnitureTagView::priority))
                .map(ft -> FurnitureCategoriesResponse.FurnitureCategoryResponse.of(
                        ft.furnitureId(),
                        ft.furnitureNameKr()
                ))
                .toList();

        List<FurnitureCategoriesResponse.FurnitureCategoryResponse> fromExtra = extraFurnitures.stream()
                .map(f -> FurnitureCategoriesResponse.FurnitureCategoryResponse.of(
                        f.getId(),
                        f.getFurnitureNameKr()
                ))
                .toList();

        List<FurnitureCategoriesResponse.FurnitureCategoryResponse> combined = new ArrayList<>();
        combined.addAll(fromTags);
        combined.addAll(fromExtra);

        return FurnitureCategoriesResponse.of(combined);
    }
}
