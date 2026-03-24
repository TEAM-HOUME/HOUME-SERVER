package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.model.entity.CreditStatus;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.Jjym;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageUsedProduct;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.generateImage.repository.GenerateImageUsedProductRepository;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.house.repository.taste.tag.TagRepository;
import or.sopt.houme.domain.preference.model.entity.Factor;
import or.sopt.houme.domain.preference.model.entity.GenerateImagePreference;
import or.sopt.houme.domain.preference.model.entity.Preference;
import or.sopt.houme.domain.preference.model.entity.PreferenceFactor;
import or.sopt.houme.domain.preference.repository.FactorRepository;
import or.sopt.houme.domain.preference.repository.GenerateImagePreferenceRepository;
import or.sopt.houme.domain.preference.repository.PreferenceFactorRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.presentation.controller.dto.ImageHistoriesResultPageResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageGeneratedImageV2Response;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.UserImageHistoryDTO;
import or.sopt.houme.domain.user.presentation.controller.dto.UserImageHistoryListResponse;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CreditException;
import or.sopt.houme.global.api.handler.GenerateImageException;
import or.sopt.houme.global.api.handler.HouseException;
import or.sopt.houme.global.api.handler.TagException;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final int SIGN_UP_CREDIT_COUNT = 1;

    private final UserRepository userRepository;
    private final HouseRepository houseRepository;
    private final TagRepository tagRepository;
    private final GenerateImageRepository generateImageRepository;
    private final CreditRepository creditRepository;
    private final GenerateImagePreferenceRepository generateImagePreferenceRepository;
    private final FactorRepository factorRepository;
    private final PreferenceRepository preferenceRepository;
    private final PreferenceFactorRepository preferenceFactorRepository;
    private final BannerRepository bannerRepository;
    private final GenerateImageUsedProductRepository generateImageUsedProductRepository;
    private final RecommendFurnitureRepository recommendFurnitureRepository;
    private final JjymRepository jjymRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;

    @Override
    @Transactional(readOnly = true)
    public MyPageInfoResponse getMyPageInfo(User user) {
        User findUser = findUser(user);
        String name = findUser.getDisplayName();
        Long creditCount = userRepository.countByMemberIdAndStatus(findUser.getId());
        return MyPageInfoResponse.of(findUser.getId(), name, creditCount);
    }

    @Override
    @Transactional(readOnly = true)
    public UserImageHistoryListResponse getUserImageHistoryList(User user) {
        User findUser = findUser(user);

        // 1. 유저가 생성한 House 목록 조회 (isValid == true)
        List<House> houses = houseRepository.findValidHouseByUserId(findUser.getId());

        List<UserImageHistoryDTO> histories = new ArrayList<>();

        for (House house : houses) {
            // 2. 각 house에 연결된 이미지가 없으면 skip
            Optional<GenerateImage> generateImage = generateImageRepository.findByHouseId(house.getId());
            if (generateImage.isEmpty()) continue;

            // 3. 해당 house에서 가장 많이 등장한 태그 가져오기
            Optional<Tag> representativeTag = tagRepository.findMostFrequentTagByHouseId(house.getId());
            if (representativeTag.isEmpty()) continue;

            // 가구 도면 객체 조회 및 isMirror(= isReverse) 값 결정
            List<HouseFloorPlan> houseFloorPlans = house.getHouseFloorPlans();
            boolean isMirror = houseFloorPlans != null && !houseFloorPlans.isEmpty() && houseFloorPlans.get(0).isReverse();

            // 4. DTO 생성
            UserImageHistoryDTO dto = new UserImageHistoryDTO(
                    house.getId(),
                    generateImage.get().getId(),
                    generateImage.get().getUrl(),
                    representativeTag.get().getTagNameKr(),
                    house.getEquilibrium(),
                    house.getForm(),
                    isMirror
            );
            histories.add(dto);
        }

        return UserImageHistoryListResponse.of(histories);
    }

    @Override
    @Transactional(readOnly = true)
    public MyPageGeneratedImageV2Response getUserGeneratedImageHistoryListV2(User user) {
        User findUser = findUser(user);
        List<GenerateImage> generateImages = generateImageRepository.findAllByUserIdWithHouseAndBanner(findUser.getId());

        if (generateImages.isEmpty()) {
            return MyPageGeneratedImageV2Response.of(List.of());
        }

        Map<Long, Banner> bannersById = buildBannerMap(generateImages);
        Map<Long, List<CurationRawProduct>> rawProductsByImageId = buildRawProductsByImageId(generateImages, bannersById);
        Map<Long, List<String>> colorsByRawProductId = buildColorsByRawProductId(rawProductsByImageId);
        Map<Long, Boolean> jjymByRawProductId = buildJjymByRawProductId(findUser.getId(), rawProductsByImageId);

        Map<LocalDate, List<MyPageGeneratedImageV2Response.ItemResponse>> grouped = new LinkedHashMap<>();
        for (GenerateImage generateImage : generateImages) {
            List<CurationRawProduct> rawProducts = rawProductsByImageId.getOrDefault(generateImage.getId(), List.of());
            List<MyPageGeneratedImageV2Response.UsedProductResponse> usedProducts = rawProducts.stream()
                    .map(rawProduct -> MyPageGeneratedImageV2Response.UsedProductResponse.of(
                            rawProduct.getId(),
                            rawProduct.getProductImageUrl(),
                            colorsByRawProductId.getOrDefault(rawProduct.getId(), List.of()),
                            rawProduct.getProductName(),
                            rawProduct.getListPrice(),
                            rawProduct.getDiscountRate(),
                            rawProduct.getDiscountPrice(),
                            rawProduct.getProductSiteUrl(),
                            jjymByRawProductId.getOrDefault(rawProduct.getId(), Boolean.FALSE)
                    ))
                    .toList();

            GenerateImageType generationType = generateImage.getResolvedGenerationType();
            Banner banner = resolveBanner(generateImage, bannersById);
            MyPageGeneratedImageV2Response.ItemResponse item = MyPageGeneratedImageV2Response.ItemResponse.of(
                    generateImage.getId(),
                    generationType == GenerateImageType.REGULAR
                            ? MyPageGeneratedImageV2Response.ViewType.RECOMMEND
                            : MyPageGeneratedImageV2Response.ViewType.LIST,
                    generateImage.getUrl(),
                    generateImage.getCreatedAt(),
                    banner != null ? banner.getBannerTitle() : null,
                    buildProductSummaryText(rawProducts),
                    usedProducts
            );

            LocalDate date = generateImage.getCreatedAt().toLocalDate();
            grouped.computeIfAbsent(date, ignored -> new ArrayList<>()).add(item);
        }

        List<MyPageGeneratedImageV2Response.DateGroupResponse> groups = grouped.entrySet().stream()
                .map(entry -> MyPageGeneratedImageV2Response.DateGroupResponse.of(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();

        return MyPageGeneratedImageV2Response.of(groups);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageHistoriesResultPageResponse getImageHistoryResultPage(User user, Long houseId) {
        User findUser = findUser(user);

        // 1. house, tag 조회
        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE_ENTITY));

        // 2. houseId 에 해당하는 generateImage 리스트 조회
        List<GenerateImage> generateImages = generateImageRepository.findGenerateImagesByHouseId(house.getId());
        if (generateImages.isEmpty()) {
            throw new GenerateImageException(ErrorCode.NOT_FOUND_GENERATE_IMAGE_ENTITY);
        }

        List<Boolean> likes = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();
        // 선택했던 factor 조회
        List<Factor> factors = new ArrayList<>();

        // 좋아요 객체
        Optional<Preference> preference;

        // 3. 최신 GenerateImagePreference 조회 (선호 여부)
        for (GenerateImage generateImage : generateImages) {
            Optional<GenerateImagePreference> optionalGenerateImagePreference =
                    generateImagePreferenceRepository.findFirstByGenerateImageIdOrderByIdDesc(generateImage.getId());

            if (optionalGenerateImagePreference.isPresent()){
                likes.add(optionalGenerateImagePreference.get().getPreference().isLike());
            } else {
                likes.add(null);
            }

            tags.add(tagRepository.findTagByUserIdAndImageId(user.getId(), generateImage.getId())
                    .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY)));

            // Preference 찾기
            preference = preferenceRepository.findPreferenceByUserIdAndImageId(findUser.getId(), generateImage.getId());

            // Factor 관련
            if (preference.isPresent()){
                PreferenceFactor preferenceFactor = preferenceFactorRepository.findByPreference(preference.get())
                        .orElse(null);

                if (preferenceFactor != null){
                    factors.add(factorRepository.findById(preferenceFactor.getFactor().getId())
                            .orElse(null));
                } else {
                    factors.add(null);
                }
            } else {
                factors.add(null);
            }
        }

        // 4. GenerateImage 리스트와 likes 리스트를 함께 사용하여 DTO 변환
        List<ImageHistoriesResultPageResponse.ImageHistoryResultPageResponse> histories =
                IntStream.range(0, generateImages.size()) // 인덱스를 활용하여 스트림 생성
                        .mapToObj(i -> {
                            GenerateImage generateImage = generateImages.get(i);
                            Boolean isLike = likes.get(i); // likes 리스트에서 해당 인덱스의 값 가져오기
                            Tag tag = tags.get(i);
                            Factor factor = factors.get(i);

                            return ImageHistoriesResultPageResponse.ImageHistoryResultPageResponse.of(
                                    generateImage.getId(),
                                    house.getEquilibrium().getDescription(),
                                    house.getForm().toString(),
                                    tag.getTagNameKr(),
                                    findUser.getDisplayName(),
                                    generateImage.getUrl(),
                                    isLike,
                                    factor == null ? null : factor.getId(),
                                    factor == null ? null : factor.getFactorText()
                            );
                        })
                        .toList();

        // 5. 응답 DTO 생성
        return ImageHistoriesResultPageResponse.of(histories);
    }

    @Override
    public String updateUser(User user, String name, Gender gender, LocalDate birthday) {

        User findUser = findUser(user);
        findUser.updateUserFromSignUp(name, birthday, gender);

        return createSignUpCreditAndGetDisplayName(findUser);
    }

    @Override
    public String updateUserV2(User user, String nickname, Gender gender, LocalDate birthday) {
        User findUser = findUser(user);
        findUser.updateUserFromSignUpV2(nickname, birthday, gender);

        return createSignUpCreditAndGetDisplayName(findUser);
    }

    private String createSignUpCreditAndGetDisplayName(User findUser) {

        try {
            List<Credit> newCredits = IntStream.range(0, SIGN_UP_CREDIT_COUNT)
                    .mapToObj(i -> Credit.builder()
                            .status(CreditStatus.ACTIVE)
                            .user(findUser)
                            .build())
                    .toList();
            creditRepository.saveAll(newCredits);
        }catch (Exception e) {
            throw new CreditException(ErrorCode.CREDIT_CREATE_EXCEPTION);
        }

        return findUser.getDisplayName();
    }

    // 이미지 생성 이력 저장
    @Transactional
    @Override
    public void updateHasGeneratedImage(User user) {
        user.updateHasGeneratedImage();

        userRepository.save(user);
    }

    private Map<Long, Banner> buildBannerMap(List<GenerateImage> generateImages) {
        Set<Long> bannerIds = generateImages.stream()
                .map(GenerateImage::getBanner)
                .filter(Objects::nonNull)
                .map(Banner::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (bannerIds.isEmpty()) {
            return Map.of();
        }

        return fetchBanners(bannerIds);
    }

    private Map<Long, Banner> fetchBanners(Set<Long> bannerIds) {
        return bannerRepository.findAllByIdInWithRawProducts(List.copyOf(bannerIds)).stream()
                .collect(Collectors.toMap(
                        Banner::getId,
                        banner -> banner,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Map<Long, List<CurationRawProduct>> buildRawProductsByImageId(
            List<GenerateImage> generateImages,
            Map<Long, Banner> bannersById
    ) {
        Map<Long, List<CurationRawProduct>> rawProductsByImageId = new LinkedHashMap<>();
        List<Long> regularImageIds = new ArrayList<>();

        for (GenerateImage generateImage : generateImages) {
            if (generateImage.getResolvedGenerationType() == GenerateImageType.REGULAR) {
                regularImageIds.add(generateImage.getId());
                continue;
            }

            Banner banner = resolveBanner(generateImage, bannersById);
            if (banner == null) {
                rawProductsByImageId.put(generateImage.getId(), List.of());
                continue;
            }

            List<CurationRawProduct> rawProducts = banner.getBannerRawProducts().stream()
                    .map(mapping -> mapping.getCurationRawProduct())
                    .filter(Objects::nonNull)
                    .toList();
            rawProductsByImageId.put(generateImage.getId(), rawProducts);
        }

        if (!regularImageIds.isEmpty()) {
            List<GenerateImageUsedProduct> mappings = generateImageUsedProductRepository.findAllByGenerateImageIdInWithRawProduct(regularImageIds);
            Map<Long, List<CurationRawProduct>> regularProductsByImageId = mappings.stream()
                    .collect(Collectors.groupingBy(
                            mapping -> mapping.getGenerateImage().getId(),
                            LinkedHashMap::new,
                            Collectors.mapping(GenerateImageUsedProduct::getCurationRawProduct, Collectors.toList())
                    ));

            for (Long imageId : regularImageIds) {
                rawProductsByImageId.put(imageId, regularProductsByImageId.getOrDefault(imageId, List.of()));
            }
        }

        return rawProductsByImageId;
    }

    private Map<Long, List<String>> buildColorsByRawProductId(Map<Long, List<CurationRawProduct>> rawProductsByImageId) {
        List<Long> rawProductIds = rawProductsByImageId.values().stream()
                .flatMap(List::stream)
                .map(CurationRawProduct::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (rawProductIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Set<String>> colorSetByRawProductId = new LinkedHashMap<>();
        List<CurationRawProductColor> colors = curationRawProductColorRepository.findAllByCurationRawProductIdIn(rawProductIds);
        for (CurationRawProductColor color : colors) {
            Long rawProductId = color.getCurationRawProduct().getId();
            String displayColor = color.getClientColorName();
            if (displayColor == null || displayColor.isBlank()) {
                displayColor = color.getRawColorName();
            }
            if (displayColor == null || displayColor.isBlank()) {
                continue;
            }
            colorSetByRawProductId
                    .computeIfAbsent(rawProductId, ignored -> new LinkedHashSet<>())
                    .add(displayColor);
        }

        return colorSetByRawProductId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())
                ));
    }

    private Map<Long, Boolean> buildJjymByRawProductId(Long userId, Map<Long, List<CurationRawProduct>> rawProductsByImageId) {
        List<CurationRawProduct> rawProducts = rawProductsByImageId.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        if (rawProducts.isEmpty()) {
            return Map.of();
        }

        List<Long> productIds = rawProducts.stream()
                .map(CurationRawProduct::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> recommendFurnitureIdByProductId = recommendFurnitureRepository
                .findAllBySourceAndFurnitureProductIdIn(CurationSource.RAW, productIds)
                .stream()
                .collect(Collectors.toMap(
                        RecommendFurniture::getFurnitureProductId,
                        RecommendFurniture::getId,
                        (left, right) -> left
                ));

        List<Long> recommendFurnitureIds = recommendFurnitureIdByProductId.values().stream().distinct().toList();
        if (recommendFurnitureIds.isEmpty()) {
            return rawProducts.stream().collect(Collectors.toMap(CurationRawProduct::getId, ignored -> Boolean.FALSE));
        }

        Set<Long> jjymRecommendFurnitureIds = jjymRepository.findAllByUserIdAndRecommendFurnitureIdIn(userId, recommendFurnitureIds)
                .stream()
                .map(Jjym::getRecommendFurniture)
                .filter(Objects::nonNull)
                .map(RecommendFurniture::getId)
                .collect(Collectors.toSet());

        Map<Long, Boolean> result = new LinkedHashMap<>();
        for (CurationRawProduct rawProduct : rawProducts) {
            Long recommendFurnitureId = recommendFurnitureIdByProductId.get(rawProduct.getProductId());
            result.put(rawProduct.getId(), recommendFurnitureId != null && jjymRecommendFurnitureIds.contains(recommendFurnitureId));
        }
        return result;
    }

    private Banner resolveBanner(GenerateImage generateImage, Map<Long, Banner> bannersById) {
        Banner banner = generateImage.getBanner();
        if (banner != null) {
            return bannersById.getOrDefault(banner.getId(), banner);
        }
        return null;
    }

    private String buildProductSummaryText(List<CurationRawProduct> rawProducts) {
        if (rawProducts == null || rawProducts.isEmpty()) {
            return null;
        }

        String firstName = rawProducts.get(0).getProductName();
        int remainingCount = rawProducts.size() - 1;
        if (remainingCount <= 0) {
            return firstName + "로 생성된 이미지";
        }
        return firstName + " 외 " + remainingCount + "개로 생성된 이미지";
    }

    private User findUser(User user) {
        return userRepository.findById(user.getId()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }
}
