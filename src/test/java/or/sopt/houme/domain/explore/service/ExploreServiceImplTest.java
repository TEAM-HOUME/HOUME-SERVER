package or.sopt.houme.domain.explore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.model.vo.BannerStyleAnswerChip;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerDetailResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.ExploreHouseTemplateDetailResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.ExploreHouseTemplateListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.OtherStyleDetailResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.OtherStyleListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.RecentFloorPlanItemResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.RecentFloorPlanResponse;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImageJsonCodec;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExploreServiceImplTest {

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private GenerateImageRepository generateImageRepository;

    @Mock
    private FloorPlanRepository floorPlanRepository;

    @Mock
    private FloorPlanImageJsonCodec floorPlanImageJsonCodec;

    @InjectMocks
    private ExploreServiceImpl exploreService;

    @Test
    @DisplayName("배너 전체 조회 시 요청한 bannerId부터 circular 순서로 반환한다")
    void getExploreBanners_returnsCircularOrder() {
        Banner firstBanner = Banner.builder().id(1L).bannerTitle("첫 번째").bannerImageUrl("https://google.com/1").build();
        Banner secondBanner = Banner.builder().id(2L).bannerTitle("두 번째").bannerImageUrl("https://google.com/2").build();
        Banner fifthBanner = Banner.builder().id(5L).bannerTitle("다섯 번째").bannerImageUrl("https://google.com/5").build();
        Banner seventhBanner = Banner.builder().id(7L).bannerTitle("일곱 번째").bannerImageUrl("https://google.com/7").build();
        Banner ninthBanner = Banner.builder().id(9L).bannerTitle("아홉 번째").bannerImageUrl("https://google.com/9").build();
        when(bannerRepository.findAllWithRawProducts(BannerType.BANNER, false))
                .thenReturn(List.of(firstBanner, secondBanner, fifthBanner, seventhBanner, ninthBanner));

        BannerExploreListResponse result = exploreService.getExploreBanners(5L);

        assertEquals(List.of(5L, 7L, 9L, 1L, 2L),
                result.banners().stream().map(banner -> banner.id()).toList());
    }

    @Test
    @DisplayName("배너 전체 조회 시 요청한 bannerId가 없으면 NOT_FOUND_BANNER 예외가 발생한다")
    void getExploreBanners_throwsWhenBannerIdNotFound() {
        Banner firstBanner = Banner.builder().id(1L).bannerTitle("첫 번째").bannerImageUrl("https://google.com/1").build();
        Banner secondBanner = Banner.builder().id(2L).bannerTitle("두 번째").bannerImageUrl("https://google.com/2").build();
        when(bannerRepository.findAllWithRawProducts(BannerType.BANNER, false))
                .thenReturn(List.of(firstBanner, secondBanner));

        GeneralException exception = assertThrows(GeneralException.class,
                () -> exploreService.getExploreBanners(5L));

        assertEquals(ErrorCode.NOT_FOUND_BANNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("배너 디테일 조회 시 BANNER 타입의 질문과 답변 목록을 반환한다")
    void getExploreBannerDetail_returnsBannerDetail() throws Exception {
        Banner banner = Banner.builder()
                .id(1L)
                .bannerType(BannerType.BANNER)
                .bannerTitle("잦은 재택근무하기 좋은 우리 집")
                .bannerImageUrl("https://google.com")
                .styleQuestion("업무 시 어떤 책상을 선호하시나요?")
                .styleAnswerChipsJson("[{\"order\":2},{\"order\":1}]")
                .build();
        when(bannerRepository.findByIdWithRawProducts(1L, BannerType.BANNER, false))
                .thenReturn(java.util.Optional.of(banner));
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(
                        new BannerStyleAnswerChip(2, "데스크테리어 가능한 깔끔한 책상", 12L),
                        new BannerStyleAnswerChip(1, "모니터 받침대가 결합된 책상", 11L)
                ));

        BannerDetailResponse result = exploreService.getExploreBannerDetail(1L);

        assertEquals("잦은 재택근무하기 좋은 우리 집", result.bannerName());
        assertEquals("https://google.com", result.bannerImageUrl());
        assertEquals("업무 시 어떤 책상을 선호하시나요?", result.question());
        assertEquals(List.of("모니터 받침대가 결합된 책상", "데스크테리어 가능한 깔끔한 책상"),
                result.answers().stream().map(answer -> answer.text()).toList());
    }

    @Test
    @DisplayName("배너 디테일 조회 시 BANNER 타입 배너가 없으면 NOT_FOUND_BANNER 예외가 발생한다")
    void getExploreBannerDetail_throwsWhenBannerNotFound() {
        when(bannerRepository.findByIdWithRawProducts(99L, BannerType.BANNER, false))
                .thenReturn(java.util.Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class,
                () -> exploreService.getExploreBannerDetail(99L));

        assertEquals(ErrorCode.NOT_FOUND_BANNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("다른 스타일 전체 조회 시 STYLE 타입만 size 만큼 반환한다")
    void getOtherStyles_returnsLimitedStyles() {
        Banner styleOne = Banner.builder().id(1L).bannerType(BannerType.STYLE).bannerTitle("미니멀한 개발자의 집").bannerImageUrl("https://google.com/1").build();
        Banner styleTwo = Banner.builder().id(2L).bannerType(BannerType.STYLE).bannerTitle("웜톤 우드 하우스").bannerImageUrl("https://google.com/2").build();
        Banner styleThree = Banner.builder().id(3L).bannerType(BannerType.STYLE).bannerTitle("하이틴 스튜디오").bannerImageUrl("https://google.com/3").build();
        when(bannerRepository.findAllWithRawProducts(BannerType.STYLE, false))
                .thenReturn(List.of(styleOne, styleTwo, styleThree));

        OtherStyleListResponse result = exploreService.getOtherStyles(2);

        assertEquals(2, result.otherStyles().size());
        assertEquals(List.of(1L, 2L), result.otherStyles().stream().map(style -> style.id()).toList());
    }

    @Test
    @DisplayName("다른 스타일 전체 조회 시 size가 없으면 전체 STYLE 목록을 반환한다")
    void getOtherStyles_returnsAllWhenSizeNull() {
        Banner styleOne = Banner.builder().id(1L).bannerType(BannerType.STYLE).bannerTitle("미니멀한 개발자의 집").bannerImageUrl("https://google.com/1").build();
        Banner styleTwo = Banner.builder().id(2L).bannerType(BannerType.STYLE).bannerTitle("웜톤 우드 하우스").bannerImageUrl("https://google.com/2").build();
        Banner styleThree = Banner.builder().id(3L).bannerType(BannerType.STYLE).bannerTitle("하이틴 스튜디오").bannerImageUrl("https://google.com/3").build();
        when(bannerRepository.findAllWithRawProducts(BannerType.STYLE, false))
                .thenReturn(List.of(styleOne, styleTwo, styleThree));

        OtherStyleListResponse result = exploreService.getOtherStyles(null);

        assertEquals(3, result.otherStyles().size());
        assertEquals(List.of(1L, 2L, 3L), result.otherStyles().stream().map(style -> style.id()).toList());
    }

    @Test
    @DisplayName("다른 스타일 전체 조회 시 size가 1 미만이면 NOT_VALID_EXCEPTION 예외가 발생한다")
    void getOtherStyles_throwsWhenSizeInvalid() {
        GeneralException exception = assertThrows(GeneralException.class,
                () -> exploreService.getOtherStyles(0));

        assertEquals(ErrorCode.NOT_VALID_EXCEPTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("스타일 디테일 조회 시 STYLE 타입 배너와 매핑 상품 목록을 반환한다")
    void getOtherStyleDetail_returnsMappedProducts() {
        CurationRawProduct product = CurationRawProduct.builder()
                .id(10L)
                .productName("리샘 코지 저상형 평상형 무헤드 침대(SS/Q) 매트리스 선택")
                .productImageUrl("https://google.com/product")
                .listPrice(39900L)
                .discountRate(30)
                .discountPrice(27990L)
                .productSiteUrl("https://google.com/link")
                .build();
        Banner style = Banner.builder()
                .id(1L)
                .bannerType(BannerType.STYLE)
                .bannerTitle("미니멀한 개발자의 집")
                .bannerImageUrl("https://google.com/style")
                .styleDescription("블랙을 중심으로 모노톤 인테리어 스타일")
                .bannerRawProducts(List.of(
                        BannerCurationRawProduct.builder().id(2L).curationRawProduct(product).build()
                ))
                .build();
        when(bannerRepository.findByIdWithRawProducts(1L, BannerType.STYLE, false))
                .thenReturn(java.util.Optional.of(style));

        OtherStyleDetailResponse result = exploreService.getOtherStyleDetail(1L);

        assertEquals("미니멀한 개발자의 집", result.styleName());
        assertEquals("https://google.com/style", result.styleImageUrl());
        assertEquals("블랙을 중심으로 모노톤 인테리어 스타일", result.styleDescription());
        assertEquals(1, result.products().size());
        assertEquals(10L, result.products().get(0).id());
        assertEquals(39900L, result.products().get(0).originalPrice());
        assertEquals(27990L, result.products().get(0).finalPrice());
    }

    @Test
    @DisplayName("스타일 디테일 조회 시 STYLE 타입 배너가 없으면 NOT_FOUND_STYLE 예외가 발생한다")
    void getOtherStyleDetail_throwsWhenStyleNotFound() {
        when(bannerRepository.findByIdWithRawProducts(99L, BannerType.STYLE, false))
                .thenReturn(java.util.Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class,
                () -> exploreService.getOtherStyleDetail(99L));

        assertEquals(ErrorCode.NOT_FOUND_STYLE, exception.getErrorCode());
    }

    @Test
    @DisplayName("최근 사용한 도면 데이터 조회 시 최근 생성 이력이 없으면 hasRecentImage=false를 반환한다")
    void getRecentFloorPlan_returnsNoRecentWhenNoGenerateImage() {
        User user = User.builder().id(1L).role(Role.ROLE_USER).build();
        when(generateImageRepository.findMostRecentByUserId(1L))
                .thenReturn(java.util.Optional.empty());

        RecentFloorPlanResponse result = exploreService.getRecentFloorPlan(user);

        assertEquals(false, result.hasRecentImage());
        assertEquals(false, result.floorPlan());
    }

    @Test
    @DisplayName("최근 사용한 도면 데이터 조회 시 최신 생성 이력의 도면 정보를 반환한다")
    void getRecentFloorPlan_returnsRecentFloorPlan() {
        User user = User.builder().id(1L).role(Role.ROLE_USER).build();
        FloorPlan floorPlan = FloorPlan.builder()
                .id(3L)
                .floorPlanName("다용도실이 있는 원룸")
                .equilibrium(Equilibrium.BETWEEN_6_10)
                .imagesJson("[{\"url\":\"https://google.com/image1\"}]")
                .url("https://google.com/fallback")
                .build();
        House house = House.builder().id(10L).houseFloorPlans(new java.util.ArrayList<>()).build();
        HouseFloorPlan houseFloorPlan = HouseFloorPlan.builder()
                .id(1L)
                .house(house)
                .floorPlan(floorPlan)
                .isReverse(false)
                .build();
        house.getHouseFloorPlans().add(houseFloorPlan);
        GenerateImage generateImage = GenerateImage.builder()
                .id(100L)
                .house(house)
                .url("https://google.com/generated")
                .filename("generated.png")
                .originalFilename("generated-original.png")
                .fileExtension("png")
                .build();

        when(generateImageRepository.findMostRecentByUserId(1L))
                .thenReturn(java.util.Optional.of(generateImage));
        when(floorPlanImageJsonCodec.read("[{\"url\":\"https://google.com/image1\"}]"))
                .thenReturn(List.of(
                        new FloorPlanImageItem("https://google.com/image1", "fp-1.png", "fp-origin-1.png", "png", 1, "창가 뷰")
                ));

        RecentFloorPlanResponse result = exploreService.getRecentFloorPlan(user);

        assertEquals(true, result.hasRecentImage());
        RecentFloorPlanItemResponse floorPlanData = (RecentFloorPlanItemResponse) result.floorPlan();
        assertEquals(3L, floorPlanData.id());
        assertEquals("다용도실이 있는 원룸", floorPlanData.name());
        assertEquals("https://google.com/image1", floorPlanData.imageUrl());
        assertEquals("6~10평", floorPlanData.equilibrium());
        assertEquals("창가 뷰", floorPlanData.view());
    }

    @Test
    @DisplayName("도면 전체 조회는 정확히 일치하는 필터 결과를 반환하고 최근 도면만 isLatest=true 처리한다")
    void getExploreHouseTemplates_returnsExactFilteredResult() {
        User user = User.builder().id(1L).role(Role.ROLE_USER).build();

        FloorPlan first = FloorPlan.builder()
                .id(1L)
                .floorPlanName("일자형 원룸")
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.BETWEEN_6_10)
                .url("https://img/1")
                .filename("f1.png")
                .originalFilename("of1.png")
                .fileExtension("png")
                .imagesJson("[{\"url\":\"https://img/1\",\"view\":\"창가 뷰\"}]")
                .build();
        FloorPlan second = FloorPlan.builder()
                .id(2L)
                .floorPlanName("분리형 원룸")
                .form(Form.VILLA)
                .structure(Structure.SEPARATED_ONE_ROOM)
                .equilibrium(Equilibrium.BETWEEN_6_10)
                .url("https://img/2")
                .filename("f2.png")
                .originalFilename("of2.png")
                .fileExtension("png")
                .imagesJson("[{\"url\":\"https://img/2\",\"view\":\"복도 뷰\"}]")
                .build();

        House house = House.builder().id(10L).houseFloorPlans(new java.util.ArrayList<>()).build();
        house.getHouseFloorPlans().add(HouseFloorPlan.builder().id(1L).house(house).floorPlan(first).isReverse(false).build());
        GenerateImage recentImage = GenerateImage.builder()
                .id(100L)
                .house(house)
                .url("https://generated")
                .filename("generated.png")
                .originalFilename("generated-origin.png")
                .fileExtension("png")
                .build();

        when(floorPlanRepository.findAll()).thenReturn(List.of(first, second));
        when(generateImageRepository.findMostRecentByUserId(1L)).thenReturn(java.util.Optional.of(recentImage));
        when(floorPlanImageJsonCodec.read(first.getImagesJson()))
                .thenReturn(List.of(new FloorPlanImageItem("https://img/1", "f1.png", "of1.png", "png", 1, "창가 뷰")));

        ExploreHouseTemplateListResponse result = exploreService.getExploreHouseTemplates(
                null,
                Form.OFFICETEL,
                Structure.OPEN_ONE_ROOM,
                Equilibrium.BETWEEN_6_10,
                user
        );

        assertEquals(true, result.isExact());
        assertEquals(1, result.floorPlans().size());
        assertEquals(1L, result.floorPlans().getFirst().id());
        assertEquals(true, result.floorPlans().getFirst().isLatest());
    }

    @Test
    @DisplayName("도면 전체 조회는 exact 결과가 없으면 평형을 제외한 residence/layout 합집합 추천을 반환한다")
    void getExploreHouseTemplates_returnsSimilarWhenExactNotFound() {
        FloorPlan byResidence = FloorPlan.builder()
                .id(1L)
                .floorPlanName("오피스텔형")
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .url("https://img/1")
                .filename("f1.png")
                .originalFilename("of1.png")
                .fileExtension("png")
                .imagesJson("[{\"url\":\"https://img/1\",\"view\":\"창가 뷰\"}]")
                .build();
        FloorPlan byLayout = FloorPlan.builder()
                .id(2L)
                .floorPlanName("분리형")
                .form(Form.VILLA)
                .structure(Structure.SEPARATED_ONE_ROOM)
                .equilibrium(Equilibrium.OVER_16)
                .url("https://img/2")
                .filename("f2.png")
                .originalFilename("of2.png")
                .fileExtension("png")
                .imagesJson("[{\"url\":\"https://img/2\",\"view\":\"복도 뷰\"}]")
                .build();
        FloorPlan other = FloorPlan.builder()
                .id(3L)
                .floorPlanName("기타")
                .form(Form.APARTMENT)
                .structure(Structure.TWO_ROOM)
                .equilibrium(Equilibrium.BETWEEN_11_15)
                .url("https://img/3")
                .filename("f3.png")
                .originalFilename("of3.png")
                .fileExtension("png")
                .imagesJson("[{\"url\":\"https://img/3\",\"view\":\"테라스 뷰\"}]")
                .build();

        when(floorPlanRepository.findAll()).thenReturn(List.of(byResidence, byLayout, other));
        when(floorPlanImageJsonCodec.read(byResidence.getImagesJson()))
                .thenReturn(List.of(new FloorPlanImageItem("https://img/1", "f1.png", "of1.png", "png", 1, "창가 뷰")));
        when(floorPlanImageJsonCodec.read(byLayout.getImagesJson()))
                .thenReturn(List.of(new FloorPlanImageItem("https://img/2", "f2.png", "of2.png", "png", 1, "복도 뷰")));

        ExploreHouseTemplateListResponse result = exploreService.getExploreHouseTemplates(
                null,
                Form.OFFICETEL,
                Structure.SEPARATED_ONE_ROOM,
                Equilibrium.BETWEEN_6_10,
                null
        );

        assertEquals(false, result.isExact());
        assertEquals(List.of(1L, 2L), result.floorPlans().stream().map(item -> item.id()).toList());
        assertEquals(false, result.floorPlans().getFirst().isLatest());
    }

    @Test
    @DisplayName("도면 상세 조회 시 images_json의 각 이미지 정보를 리스트로 반환한다")
    void getExploreHouseTemplateDetail_returnsFloorPlanDetailFromImagesJson() {
        FloorPlan floorPlan = FloorPlan.builder()
                .id(1L)
                .floorPlanName("다용도실이 있는 원룸")
                .equilibrium(Equilibrium.BETWEEN_6_10)
                .url("https://img/fallback")
                .filename("fallback.png")
                .originalFilename("fallback-origin.png")
                .fileExtension("png")
                .imagesJson("[{\"url\":\"https://img/1\",\"view\":\"창가 뷰\"}]")
                .build();

        when(floorPlanRepository.findById(1L)).thenReturn(java.util.Optional.of(floorPlan));
        when(floorPlanImageJsonCodec.read(floorPlan.getImagesJson()))
                .thenReturn(List.of(
                        new FloorPlanImageItem("https://img/1", "f1.png", "of1.png", "png", 1, "창가 뷰"),
                        new FloorPlanImageItem("https://img/2", "f2.png", "of2.png", "png", 2, "주방 뷰")
                ));

        ExploreHouseTemplateDetailResponse result = exploreService.getExploreHouseTemplateDetail(1L);

        assertEquals(1L, result.floorPlanId());
        assertEquals("다용도실이 있는 원룸", result.floorPlanName());
        assertEquals("6~10평", result.equilibrium());
        assertEquals(2, result.floorPlans().size());
        assertEquals("https://img/1", result.floorPlans().getFirst().imageUrl());
        assertEquals("창가 뷰", result.floorPlans().getFirst().view());
    }
}
