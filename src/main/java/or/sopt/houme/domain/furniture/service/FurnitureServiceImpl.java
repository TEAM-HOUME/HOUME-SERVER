package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.client.FastApiImageHashClient;
import or.sopt.houme.domain.furniture.client.NaverShopApiClient;
import or.sopt.houme.domain.furniture.dto.ActivityItem;
import or.sopt.houme.domain.furniture.dto.FurnitureItem;
import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureCategoryGroup;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.entity.FurnitureType;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.tag.TagRepository;
import or.sopt.houme.domain.user.entity.User;
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

    private final NaverShopApiClient naverShopApiClient;
    private final FastApiImageHashClient imageHashClient;

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
                        Collectors.collectingAndThen(
                                Collectors.mapping(FurnitureItem::from, Collectors.toList()),
                                list -> {
                                    list.sort(Comparator.comparing(FurnitureItem::id, Comparator.nullsLast(Comparator.naturalOrder())));     // FurnitureItem 리스트 id 기준으로 정렬
                                    return list;
                                }
                        )
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

    @Override
    public FurnitureCategoriesResponse getFurnitureCategoriesByStyle(User user, Long imageId, List<String> detectedObjects) {

        // 1. userId와 imageId로 해당하는 스타일 태그 조회
        Tag tag = tagRepository.findTagByUserIdAndImageId(user.getId(), imageId).orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        // 2. userId와 imageId로 이미지 생성시 선택했던 가구들을 조회
        House house = houseRepository.findHouseByUserIdAndImageId(user.getId(), imageId).orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE_ENTITY));
        List<Furniture> selectedFurnitures = furnitureRepository.findAllByHouseId(house.getId());

        // 3. 2번 과정에서 생성된 selectedFurnitures의 'object365Word' 필드와 FurnitureCategoriesRequest의 furnitureNames를 비교하여 교집합 산출
        Set<String> requestedObjects = detectedObjects.stream()
                .map(String::toLowerCase) // 소문자로 변환
                .collect(Collectors.toSet());

        List<Furniture> intersectedFurnitures = selectedFurnitures.stream()
                .filter(f -> f.getObject365Word() != null
                        && requestedObjects.contains(f.getObject365Word().toLowerCase()))  // 소문자로 비교하기
                .toList();

        // 4. 교집합으로 산출된 가구들과 스타일 태그에 해당하는 매핑 객체를 furniture_tags에서 조회
        List<FurnitureTag> styleMappedFurnitureTags = furnitureTagRepository.findAllByTagIdAndFurnitureIn(
                tag.getId(),
                intersectedFurnitures
        );

        // 5. priority 기준 오름차순 정렬 → 응답 DTO 변환
        List<FurnitureCategoriesResponse.FurnitureCategoryResponse> categoryResponses =
                styleMappedFurnitureTags.stream()
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
}
