package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.client.NaverShopApiClient;
import or.sopt.houme.domain.furniture.dto.ActivityItem;
import or.sopt.houme.domain.furniture.dto.FurnitureItem;
import or.sopt.houme.domain.furniture.dto.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.dto.NaverFurnitureProductDtoForPlan;
import or.sopt.houme.domain.furniture.dto.response.*;
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
import or.sopt.houme.global.util.ColorHashUtil;
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
    public FurnitureProductsInfoResponse getFurnitureProductInfoFromNaverApi(User user, Long imageId, Long categoryId) {

        // 1. userId와 imageId로 스타일 태그 조회
        Tag tag = tagRepository.findTagByUserIdAndImageId(user.getId(), imageId).orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        // 2. categoryId로 furniture 객체 조회
        Furniture furniture = furnitureRepository.findById(categoryId).orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        // 3. tagId와 categoryId(=furnitureId)로 furnitureTag 매핑 객체 조회
        FurnitureTag furnitureTag = furnitureTagRepository.findByFurnitureAndTag(furniture, tag).orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));

        // 4. 네이버 API 호출
        String searchKeyword = furnitureTag.getSearchKeyword(); // ← DB에서 매핑된 검색 키워드 꺼냄
        List<NaverFurnitureProductDto> products = naverShopApiClient.searchProducts(searchKeyword, 20);

        try {
            // 5-1. 기준 가구(furnitureTag) 컬러 해시 계산
            double[] baseColorHash = ColorHashUtil.getColorHistogramFromUrl(furnitureTag.getFurnitureUrl());

            // 5-2. products 각각의 컬러 해시 계산 및 유사도 점수 부여
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> infos = products.stream()
                    .map(dto -> {
                        try {
                            double[] productHash = ColorHashUtil.getColorHistogramFromUrl(dto.furnitureProductImageUrl());
                            double similarity = ColorHashUtil.cosineSimilarity(baseColorHash, productHash);

                            return new AbstractMap.SimpleEntry<>(
                                    similarity,
                                    FurnitureProductsInfoResponse.FurnitureProductInfo.of(
                                            dto.furnitureProductImageUrl(),
                                            dto.furnitureProductSiteUrl(),
                                            dto.furnitureProductName(),
                                            dto.furnitureProductMallName()
                                    )
                            );
                        } catch (Exception e) {
                            return new AbstractMap.SimpleEntry<>(
                                    0.0,
                                    FurnitureProductsInfoResponse.FurnitureProductInfo.of(
                                            dto.furnitureProductImageUrl(),
                                            dto.furnitureProductSiteUrl(),
                                            dto.furnitureProductName(),
                                            dto.furnitureProductMallName()
                                    )
                            );
                        }
                    })
                    .sorted((a, b) -> Double.compare(b.getKey(), a.getKey()))
                    .limit(5)
                    .map(Map.Entry::getValue)
                    .toList();

            return FurnitureProductsInfoResponse.of(user.getName(), infos);

        } catch (Exception e) {
            throw new GeneralException(ErrorCode.IMAGE_PROCESSING_ERROR);
        }
    }

    @Override
    public FurnitureProductsInfoResponseForPlan getFurnitureProductInfoFromNaverApiForPlan(User user, Long tagId, Long furnitureId, String searchKeyword, int searchProductsCount) {
        // 1. tagId로 스타일 태그 조회
        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        // 2. categoryId로 furniture 객체 조회
        Furniture furniture = furnitureRepository.findById(furnitureId).orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        // 3. tagId와 categoryId(=furnitureId)로 furnitureTag 매핑 객체 조회
        FurnitureTag furnitureTag = furnitureTagRepository.findByFurnitureAndTag(furniture, tag).orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));

        // 4. 네이버 API 호출
        List<NaverFurnitureProductDtoForPlan> products = naverShopApiClient.searchProductsForPlan(searchKeyword, searchProductsCount);

        try {
            // 5-1. 기준 가구(furnitureTag) 컬러 해시 계산
            double[] baseColorHash = ColorHashUtil.getColorHistogramFromUrl(furnitureTag.getFurnitureUrl());

            // 5-2. products 각각의 컬러 해시 계산 및 유사도 점수 부여
            List<FurnitureProductsInfoResponseForPlan.FurnitureProductInfo> infos = products.stream()
                    .map(dto -> {
                        try {
                            double[] productHash = ColorHashUtil.getColorHistogramFromUrl(dto.furnitureProductImageUrl());
                            double similarity = ColorHashUtil.cosineSimilarity(baseColorHash, productHash);

                            return new AbstractMap.SimpleEntry<>(
                                    similarity,
                                    FurnitureProductsInfoResponseForPlan.FurnitureProductInfo.of(
                                            similarity,
                                            dto.furnitureProductImageUrl(),
                                            dto.furnitureProductSiteUrl(),
                                            dto.furnitureProductName(),
                                            dto.furnitureProductMallName(),
                                            dto.furnitureProductLprice(),
                                            dto.furnitureProductId(),
                                            dto.furnitureProductBrand(),
                                            dto.furnitureProductMaker()
                                    )
                            );
                        } catch (Exception e) {
                            return new AbstractMap.SimpleEntry<>(
                                    0.0,
                                    FurnitureProductsInfoResponseForPlan.FurnitureProductInfo.of(
                                            0.0,
                                            dto.furnitureProductImageUrl(),
                                            dto.furnitureProductSiteUrl(),
                                            dto.furnitureProductName(),
                                            dto.furnitureProductMallName(),
                                            dto.furnitureProductLprice(),
                                            dto.furnitureProductId(),
                                            dto.furnitureProductBrand(),
                                            dto.furnitureProductMaker()
                                    )
                            );
                        }
                    })
                    .sorted((a, b) -> Double.compare(b.getKey(), a.getKey()))
                    .limit(searchProductsCount)
                    .map(Map.Entry::getValue)
                    .toList();

            return FurnitureProductsInfoResponseForPlan.of(user.getName(), infos);

        } catch (Exception e) {
            throw new GeneralException(ErrorCode.IMAGE_PROCESSING_ERROR);
        }
    }
}
