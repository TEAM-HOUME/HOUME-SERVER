package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.Jjym;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
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
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.repository.HouseFloorPlanRepository;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.preference.model.entity.GenerateImagePreference;
import or.sopt.houme.domain.preference.model.entity.Preference;
import or.sopt.houme.domain.preference.repository.FactorRepository;
import or.sopt.houme.domain.preference.repository.GenerateImagePreferenceRepository;
import or.sopt.houme.domain.preference.repository.PreferenceFactorRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.repository.taste.tag.TagRepository;
import or.sopt.houme.domain.user.presentation.controller.dto.*;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.*;

class UserServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final HouseRepository houseRepository = mock(HouseRepository.class);
    private final HouseFloorPlanRepository houseFloorPlanRepository = mock(HouseFloorPlanRepository.class);
    private final TagRepository tagRepository = mock(TagRepository.class);
    private final GenerateImageRepository generateImageRepository = mock(GenerateImageRepository.class);
    private final CreditRepository creditRepository = mock(CreditRepository.class);
    private final GenerateImagePreferenceRepository generateImagePreferenceRepository = mock(GenerateImagePreferenceRepository.class);
    private final FactorRepository factorRepository = mock(FactorRepository.class);
    private final PreferenceRepository preferenceRepository = mock(PreferenceRepository.class);
    private final PreferenceFactorRepository preferenceFactorRepository = mock(PreferenceFactorRepository.class);
    private final BannerRepository bannerRepository = mock(BannerRepository.class);
    private final GenerateImageUsedProductRepository generateImageUsedProductRepository = mock(GenerateImageUsedProductRepository.class);
    private final RecommendFurnitureRepository recommendFurnitureRepository = mock(RecommendFurnitureRepository.class);
    private final JjymRepository jjymRepository = mock(JjymRepository.class);
    private final CurationRawProductColorRepository curationRawProductColorRepository = mock(CurationRawProductColorRepository.class);
    private final NicknameService nicknameService = mock(NicknameService.class);
    private final UserNicknameTagTransactionService userNicknameTagTransactionService = mock(UserNicknameTagTransactionService.class);

    private final UserServiceImpl userService = new UserServiceImpl(
            userRepository,
            houseRepository,
            houseFloorPlanRepository,
            tagRepository,
            generateImageRepository,
            creditRepository,
            generateImagePreferenceRepository,
            factorRepository,
            preferenceRepository,
            preferenceFactorRepository,
            bannerRepository,
            generateImageUsedProductRepository,
            recommendFurnitureRepository,
            jjymRepository,
            curationRawProductColorRepository,
            nicknameService,
            userNicknameTagTransactionService
            );

    private User user;
    private House house;
    private Tag tag;
    private GenerateImage generateImage;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("테스트유저")
                .build();

        house = House.builder()
                .id(20L)
                .user(user)
                .build();

        tag = Tag.builder()
                .tagNameKr("모던")
                .build();

        generateImage = GenerateImage.builder()
                .id(100L)
                .url("https://cdn.com/image.png")
                .house(house)
                .build();
        ReflectionTestUtils.setField(generateImage, "createdAt", LocalDateTime.of(2026, 3, 24, 10, 0));
    }

    @Test
    @DisplayName("마이페이지 유저 정보 조회 성공")
    void getMyPageInfo_success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.countByMemberIdAndStatus(1L)).willReturn(10L);

        // when
        MyPageInfoResponse response = userService.getMyPageInfo(user);

        // then
        assertThat(response.name()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("유저 정보가 없을 경우 예외 발생")
    void getMyPageInfo_userNotFound() {
        // given
        User user = User.builder().id(99L).build();
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getMyPageInfo(user))
                .isInstanceOf(UserException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMsg());
    }

    @Test
    @DisplayName("유저의 이미지 생성 이력 조회 성공 - 동일한 houseId에 여러 이미지가 있을 때 첫 번째 것만 반환")
    void getUserImageHistoryList_MultipleGenerateImages_ReturnsFirst() {
        // given
        Long userId = user.getId();
        FloorPlan floorPlan = FloorPlan.builder()
                .form(Form.APARTMENT)
                .equilibrium(Equilibrium.UNDER_5)
                .build();

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        // 1. 유효한 house 반환
        given(houseRepository.findValidHouseByUserId(userId))
                .willReturn(List.of(house));

        // 2. 동일한 houseId에 여러 이미지가 있다고 가정
        GenerateImage firstImage = GenerateImage.builder()
                .id(100L)
                .url("https://cdn.com/image1.png")
                .house(house)
                .build();

        GenerateImage secondImage = GenerateImage.builder()
                .id(200L)
                .url("https://cdn.com/image2.png")
                .house(house)
                .build();

        // Repository는 결국 fetchFirst() 결과만 반환하므로 "첫 번째 이미지"만 리턴되도록 설정
        given(generateImageRepository.findByHouseId(house.getId()))
                .willReturn(Optional.of(firstImage));

        // 3. 대표 태그 반환
        given(tagRepository.findMostFrequentTagByHouseId(house.getId()))
                .willReturn(Optional.ofNullable(tag));
        given(houseFloorPlanRepository.findHouseFloorPlanByHouseId(house.getId()))
                .willReturn(Optional.of(HouseFloorPlan.builder().house(house).floorPlan(floorPlan).isReverse(false).build()));

        // when
        UserImageHistoryListResponse response = userService.getUserImageHistoryList(user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.histories()).hasSize(1);

        UserImageHistoryDTO dto = response.histories().get(0);
        assertThat(dto.imageId()).isEqualTo(100L); // 첫번째 이미지가 선택됨
        assertThat(dto.generatedImageUrl()).isEqualTo("https://cdn.com/image1.png");
        assertThat(dto.tasteTag()).isEqualTo("모던");
        assertThat(dto.equilibrium()).isEqualTo("5평 이하");
        assertThat(dto.houseForm()).isEqualTo("아파트");
    }

    @Test
    @DisplayName("마이페이지 이미지 히스토리 결과 페이지 - 생성된 이미지 2개 조회 성공")
    void getImageHistoryResultPage_multipleGenerateImages_success() {
        // given
        Long userId = 1L;
        Long imageId = 10L;
        Long houseId = 20L;
        User user = User.builder()
                .id(userId)
                .name("테스트유저")
                .build();

        House house = House.builder()
                .id(houseId)
                .build();
        FloorPlan floorPlan = FloorPlan.builder()
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .build();

        Tag tag = Tag.builder()
                .tagNameKr("모던")
                .build();

        GenerateImage generateImage1 = GenerateImage.builder()
                .id(1L)
                .url("https://example.com/image1.png")
                .house(house)
                .build();

        GenerateImage generateImage2 = GenerateImage.builder()
                .id(2L)
                .url("https://example.com/image2.png")
                .house(house)
                .build();

        GenerateImagePreference generateImagePreference1 = GenerateImagePreference.builder()
                .preference(Preference.builder().isLike(true).build())
                .generateImage(generateImage1)
                .build();

        GenerateImagePreference generateImagePreference2 = GenerateImagePreference.builder()
                .preference(Preference.builder().isLike(true).build())
                .generateImage(generateImage2)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(houseRepository.findById(houseId)).willReturn(Optional.of(house));

        // generateImages 리스트 2개 반환
        given(generateImageRepository.findGenerateImagesByHouseId(house.getId()))
                .willReturn(List.of(generateImage1, generateImage2));
        given(houseFloorPlanRepository.findHouseFloorPlanByHouseId(house.getId()))
                .willReturn(Optional.of(HouseFloorPlan.builder().house(house).floorPlan(floorPlan).isReverse(false).build()));

        given(generateImagePreferenceRepository.findFirstByGenerateImageIdOrderByIdDesc(generateImage1.getId()))
                .willReturn(Optional.of(generateImagePreference1));
        given(generateImagePreferenceRepository.findFirstByGenerateImageIdOrderByIdDesc(generateImage2.getId()))
                .willReturn(Optional.of(generateImagePreference2));

        given(tagRepository.findTagByUserIdAndImageId(userId, generateImage1.getId()))
                .willReturn(Optional.of(tag));
        given(tagRepository.findTagByUserIdAndImageId(userId, generateImage2.getId()))
                .willReturn(Optional.of(tag));

        // when
        ImageHistoriesResultPageResponse response = userService.getImageHistoryResultPage(user, houseId);

        // then
        assertThat(response.histories()).hasSize(2);

        ImageHistoriesResultPageResponse.ImageHistoryResultPageResponse history1 = response.histories().get(0);
        ImageHistoriesResultPageResponse.ImageHistoryResultPageResponse history2 = response.histories().get(1);

        assertThat(history1.generatedImageUrl()).isEqualTo("https://example.com/image1.png");
        assertThat(history2.generatedImageUrl()).isEqualTo("https://example.com/image2.png");

        // 공통 속성 검증
        response.histories().forEach(history -> {
            assertThat(history.equilibrium()).isEqualTo("5평 이하");
            assertThat(history.houseForm()).isEqualTo("오피스텔");
            assertThat(history.tasteTag()).isEqualTo("모던");
            assertThat(history.name()).isEqualTo("테스트유저");
            assertThat(history.isLike()).isTrue();
        });
    }

    @Test
    @DisplayName("house를 찾을 수 없는 경우 예외 발생")
    void getImageHistoryResultPage_notFoundHouse() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        // Service는 houseId로 조회하므로 findById 스텁
        given(houseRepository.findById(generateImage.getId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getImageHistoryResultPage(user, generateImage.getId()))
                .isInstanceOf(HouseException.class)
                .hasMessageContaining("집 객체를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("tag를 찾을 수 없는 경우 예외 발생")
    void getImageHistoryResultPage_notFoundTag() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(houseRepository.findById(house.getId()))
                .willReturn(Optional.of(house));
        given(generateImageRepository.findGenerateImagesByHouseId(house.getId()))
                .willReturn(List.of(generateImage));
        given(generateImagePreferenceRepository.findFirstByGenerateImageIdOrderByIdDesc(generateImage.getId()))
                .willReturn(Optional.empty());
        given(tagRepository.findTagByUserIdAndImageId(user.getId(), generateImage.getId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getImageHistoryResultPage(user, house.getId()))
                .isInstanceOf(TagException.class)
                .hasMessageContaining("태그 객체를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("generateImage를 찾을 수 없는 경우 예외 발생")
    void getImageHistoryResultPage_notFoundGenerateImage() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(houseRepository.findById(house.getId()))
                .willReturn(Optional.of(house));
        given(tagRepository.findTagByUserIdAndImageId(user.getId(), generateImage.getId()))
                .willReturn(Optional.of(tag));
        given(generateImageRepository.findGenerateImagesByHouseId(house.getId()))
                .willReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> userService.getImageHistoryResultPage(user, house.getId()))
                .isInstanceOf(GenerateImageException.class)
                .hasMessageContaining("생성된 이미지 객체를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("마이페이지 생성 이미지 이력 v2 조회 성공 - 배너/일반 생성 이미지를 날짜별로 묶어 반환한다")
    void getUserGeneratedImageHistoryListV2_success() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        CurationRawProduct bannerRawProduct = CurationRawProduct.builder()
                .id(101L)
                .source("soozip")
                .category(SoozipCategory.FURNITURE)
                .productId(1001L)
                .productImageUrl("https://cdn.com/banner-product.png")
                .productSiteUrl("https://mall/banner-product")
                .productName("배너 가구")
                .listPrice(100000L)
                .discountRate(10)
                .discountPrice(90000L)
                .fetchedAt(LocalDateTime.of(2026, 3, 24, 9, 0))
                .build();

        Banner banner = Banner.builder()
                .id(11L)
                .bannerType(BannerType.BANNER)
                .bannerImageUrl("https://cdn.com/banner.png")
                .bannerTitle("테스트 배너")
                .bannerRawProducts(new java.util.ArrayList<>())
                .build();
        banner.getBannerRawProducts().add(BannerCurationRawProduct.of(banner, bannerRawProduct));

        House houseWithBanner = House.builder()
                .id(house.getId())
                .activity(house.getActivity())
                .user(user)
                .banner(banner)
                .isValid(true)
                .build();

        GenerateImage bannerImage = GenerateImage.builder()
                .id(201L)
                .url("https://cdn.com/banner-image.png")
                .house(houseWithBanner)
                .generationType(GenerateImageType.LIST)
                .build();
        ReflectionTestUtils.setField(bannerImage, "createdAt", LocalDateTime.of(2026, 3, 24, 11, 0));

        CurationRawProduct regularRawProduct = CurationRawProduct.builder()
                .id(102L)
                .source("soozip")
                .category(SoozipCategory.FURNITURE)
                .productId(1002L)
                .productImageUrl("https://cdn.com/regular-product.png")
                .productSiteUrl("https://mall/regular-product")
                .productName("일반 가구")
                .listPrice(200000L)
                .discountRate(15)
                .discountPrice(170000L)
                .fetchedAt(LocalDateTime.of(2026, 3, 24, 9, 30))
                .build();

        GenerateImage regularImage = GenerateImage.builder()
                .id(202L)
                .url("https://cdn.com/regular-image.png")
                .house(house)
                .generationType(GenerateImageType.RECOMMEND)
                .build();
        ReflectionTestUtils.setField(regularImage, "createdAt", LocalDateTime.of(2026, 3, 24, 10, 0));

        given(generateImageRepository.findAllByUserIdWithHouseAndBanner(user.getId()))
                .willReturn(List.of(bannerImage, regularImage));
        given(bannerRepository.findAllByIdInWithRawProducts(List.of(11L)))
                .willReturn(List.of(banner));
        given(generateImageUsedProductRepository.findAllByGenerateImageIdInWithRawProduct(List.of(202L)))
                .willReturn(List.of(GenerateImageUsedProduct.of(regularImage, regularRawProduct, 1)));

        CurationRawProductColor bannerColor = CurationRawProductColor.builder()
                .id(1L)
                .curationRawProduct(bannerRawProduct)
                .rawColorName("화이트")
                .clientColorName("화이트")
                .build();
        CurationRawProductColor regularColor = CurationRawProductColor.builder()
                .id(2L)
                .curationRawProduct(regularRawProduct)
                .rawColorName("우드")
                .clientColorName("우드")
                .build();
        given(curationRawProductColorRepository.findAllByCurationRawProductIdIn(List.of(101L, 102L)))
                .willReturn(List.of(bannerColor, regularColor));

        RecommendFurniture bannerRecommendFurniture = RecommendFurniture.builder()
                .id(501L)
                .furnitureProductId(1001L)
                .source(CurationSource.RAW)
                .build();
        RecommendFurniture regularRecommendFurniture = RecommendFurniture.builder()
                .id(502L)
                .furnitureProductId(1002L)
                .source(CurationSource.RAW)
                .build();
        given(recommendFurnitureRepository.findAllBySourceAndFurnitureProductIdIn(CurationSource.RAW, List.of(1001L, 1002L)))
                .willReturn(List.of(bannerRecommendFurniture, regularRecommendFurniture));

        Jjym jjym = Jjym.builder()
                .id(1L)
                .user(user)
                .recommendFurniture(regularRecommendFurniture)
                .build();
        given(jjymRepository.findAllByUserIdAndRecommendFurnitureIdIn(user.getId(), List.of(501L, 502L)))
                .willReturn(List.of(jjym));

        // when
        MyPageGeneratedImageV2Response response = userService.getUserGeneratedImageHistoryListV2(user);

        // then
        assertThat(response.groups()).hasSize(1);
        MyPageGeneratedImageV2Response.DateGroupResponse group = response.groups().get(0);
        assertThat(group.date()).isEqualTo(LocalDate.of(2026, 3, 24));
        assertThat(group.items()).hasSize(2);

        MyPageGeneratedImageV2Response.ItemResponse firstItem = group.items().get(0);
        assertThat(firstItem.viewType()).isEqualTo(MyPageGeneratedImageV2Response.ViewType.LIST);
        assertThat(firstItem.bannerTitle()).isEqualTo("테스트 배너");
        assertThat(firstItem.productSummaryText()).isEqualTo("배너 가구로 생성된 이미지");
        assertThat(firstItem.usedProducts()).hasSize(1);
        assertThat(firstItem.usedProducts().get(0).isJjym()).isFalse();

        MyPageGeneratedImageV2Response.ItemResponse secondItem = group.items().get(1);
        assertThat(secondItem.viewType()).isEqualTo(MyPageGeneratedImageV2Response.ViewType.RECOMMEND);
        assertThat(secondItem.bannerTitle()).isNull();
        assertThat(secondItem.productSummaryText()).isEqualTo("일반 가구로 생성된 이미지");
        assertThat(secondItem.usedProducts()).hasSize(1);
        assertThat(secondItem.usedProducts().get(0).isJjym()).isTrue();
        assertThat(secondItem.usedProducts().get(0).colors()).containsExactly("우드");
    }

    @Test
    @DisplayName("성공적으로_유저정보를_업데이트한다")
    void updateUser_success() {
        // given
        String male = "MALE";

        // 요청한 유저의 Id
        User inputUser = User.builder().id(1L).build();

        // DB에 있는 유저의 필드 값들
        User dbUser = User.builder()
                .id(1L)
                .name(null)
                .birthday(null)
                .gender(null)
                .build();

        // 요청
        CreateUserRequest request = CreateUserRequest.of(
                "New Name",
                male,
                LocalDate.of(2000, 5, 15).toString()
        );

        // 유저 모킹
        given(userRepository.findById(1L)).willReturn(Optional.of(dbUser));

        // when
        userService.updateUser(inputUser, request.name(), Gender.MALE, LocalDate.of(2000, 5, 15));

        // then
        assertEquals("New Name", dbUser.getName());
        assertEquals(null, dbUser.getNickname());
        assertEquals(Gender.MALE, dbUser.getGender());
        assertEquals(LocalDate.of(2000, 5, 15), dbUser.getBirthday());
    }


    @Test
    @DisplayName("성공적으로_유저정보를_업데이트하면_크레딧을 신규로 생성한다")
    void updateUser_credit_create() {
        // given
        String male = "MALE";
        // 요청한 유저의 Id
        User inputUser = User.builder().id(1L).build();

        // DB에 있는 유저의 필드 값들
        User dbUser = User.builder()
                .id(1L)
                .name(null)
                .birthday(null)
                .gender(null)
                .build();

        // 요청
        CreateUserRequest request = CreateUserRequest.of(
                "New Name",
                male,
                LocalDate.of(2000, 5, 15).toString()
        );

        // 유저 모킹
        given(userRepository.findById(1L)).willReturn(Optional.of(dbUser));

        // when
        userService.updateUser(inputUser, request.name(), Gender.MALE, LocalDate.of(2000, 5, 15));

        // then
        assertEquals("New Name", dbUser.getName());
        assertEquals(null, dbUser.getNickname());
        assertEquals(Gender.MALE, dbUser.getGender());
        assertEquals(LocalDate.of(2000, 5, 15), dbUser.getBirthday());

        verify(creditRepository, times(1))
                .saveAll(argThat(credits -> credits instanceof java.util.Collection<?> c && c.size() == 5));
    }


    @Test
    @DisplayName("크레딧 저장 중 예외가 발생하면 CreditException을 던진다")
    void updateUser_credit_create_fail() {
        // given
        String male = "MALE";
        User inputUser = User.builder().id(1L).build();

        User dbUser = User.builder()
                .id(1L)
                .name(null)
                .birthday(null)
                .gender(null)
                .build();

        CreateUserRequest request = CreateUserRequest.of(
                "New Name",
                male,
                LocalDate.of(2000, 5, 15).toString()
        );

        // 유저 조회는 정상적으로 동작
        given(userRepository.findById(1L)).willReturn(Optional.of(dbUser));

        // 크레딧 저장 시 RuntimeException 발생하도록 설정
        willThrow(new RuntimeException("DB error"))
                .given(creditRepository).saveAll(anyList());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(inputUser, request.name(), Gender.MALE, LocalDate.of(2000, 5, 15)
        ))
                .isInstanceOf(CreditException.class)
                .hasMessageContaining("크레딧 생성 과정 중 예외가 발생하였습니다.");
    }

    @Test
    @DisplayName("v2 회원가입은 닉네임 필드를 함께 업데이트한다")
    void updateUserV2_success() {
        // given
        User inputUser = User.builder().id(1L).build();

        User dbUser = User.builder()
                .id(1L)
                .name(null)
                .nickname(null)
                .birthday(null)
                .gender(null)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(dbUser));
        given(nicknameService.generateNicknameTag("새닉네임")).willReturn("#1234");
        given(userNicknameTagTransactionService.completeUserSignUpV2(
                1L,
                "새닉네임",
                "#1234",
                Gender.MALE,
                LocalDate.of(2000, 5, 15)
        )).willAnswer(invocation -> {
            dbUser.updateUserFromSignUpV2("새닉네임", "#1234", LocalDate.of(2000, 5, 15), Gender.MALE);
            return dbUser.getDisplayName();
        });

        // when
        userService.updateUserV2(inputUser, "새닉네임", Gender.MALE, LocalDate.of(2000, 5, 15));

        // then
        assertEquals("새닉네임", dbUser.getName());
        assertEquals("새닉네임", dbUser.getNickname());
        assertEquals("#1234", dbUser.getNicknameTag());
        assertEquals(Gender.MALE, dbUser.getGender());
        assertEquals(LocalDate.of(2000, 5, 15), dbUser.getBirthday());
    }

    @Test
    @DisplayName("updateUserV2는 닉네임 태그 유니크 충돌이 나면 재시도한다")
    void updateUserV2_retryOnNicknameTagConstraintViolation() {
        User inputUser = User.builder().id(1L).build();
        User dbUser = User.builder().id(1L).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(dbUser));
        given(nicknameService.generateNicknameTag("새닉네임"))
                .willReturn("#1234", "#5678");
        given(userNicknameTagTransactionService.completeUserSignUpV2(
                1L,
                "새닉네임",
                "#1234",
                Gender.MALE,
                LocalDate.of(2000, 5, 15)
        )).willThrow(new DataIntegrityViolationException("uk_user_nickname_nickname_tag"));
        given(userNicknameTagTransactionService.completeUserSignUpV2(
                1L,
                "새닉네임",
                "#5678",
                Gender.MALE,
                LocalDate.of(2000, 5, 15)
        )).willReturn("새닉네임");

        String result = userService.updateUserV2(inputUser, "새닉네임", Gender.MALE, LocalDate.of(2000, 5, 15));

        assertEquals("새닉네임", result);
        then(nicknameService).should(times(2)).generateNicknameTag("새닉네임");
    }

}
