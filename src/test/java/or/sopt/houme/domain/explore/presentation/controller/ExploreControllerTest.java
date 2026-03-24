package or.sopt.houme.domain.explore.presentation.controller;

import or.sopt.houme.domain.explore.presentation.dto.response.BannerDetailAnswerResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerDetailResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.OtherStyleDetailProductResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.OtherStyleDetailResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.OtherStyleListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.OtherStyleResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.RecentFloorPlanItemResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.RecentFloorPlanResponse;
import or.sopt.houme.domain.explore.service.ExploreService;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExploreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExploreService exploreService;

    @Test
    @DisplayName("배너 전체 조회 API는 요청한 bannerId부터 순환 정렬된 배너 목록을 반환한다")
    void getExploreBanners_returnsCircularBanners() throws Exception {
        Mockito.when(exploreService.getExploreBanners(5L))
                .thenReturn(BannerExploreListResponse.of(List.of(
                        new BannerExploreResponse(5L, "잦은 재택근무에 딱 맞는", "https://google.com"),
                        new BannerExploreResponse(7L, "취향 탐색이 필요한", "https://google.com"),
                        new BannerExploreResponse(9L, "휴식이 필요한", "https://google.com"),
                        new BannerExploreResponse(1L, "작은 집 수납이 필요한", "https://google.com"),
                        new BannerExploreResponse(2L, "홈카페 감성이 필요한", "https://google.com")
                )));

        mockMvc.perform(get("/api/v1/explore/banners/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.banners[0].id").value(5L))
                .andExpect(jsonPath("$.data.banners[0].name").value("잦은 재택근무에 딱 맞는"))
                .andExpect(jsonPath("$.data.banners[2].id").value(9L))
                .andExpect(jsonPath("$.data.banners[3].id").value(1L))
                .andExpect(jsonPath("$.data.banners[4].id").value(2L));
    }

    @Test
    @DisplayName("배너 디테일 페이지 조회 API는 배너 상세 정보를 반환한다")
    void getExploreBannerDetail_returnsDetail() throws Exception {
        Mockito.when(exploreService.getExploreBannerDetail(1L))
                .thenReturn(BannerDetailResponse.of(
                        "잦은 재택근무하기 좋은 우리 집",
                        "https://google.com",
                        "업무 시 어떤 책상을 선호하시나요?",
                        List.of(
                                BannerDetailAnswerResponse.of("모니터 받침대가 결합된 책상"),
                                BannerDetailAnswerResponse.of("데스크테리어 가능한 깔끔한 책상")
                        )
                ));

        mockMvc.perform(get("/api/v1/explore/banners/1/detail")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.bannerName").value("잦은 재택근무하기 좋은 우리 집"))
                .andExpect(jsonPath("$.data.bannerImageUrl").value("https://google.com"))
                .andExpect(jsonPath("$.data.question").value("업무 시 어떤 책상을 선호하시나요?"))
                .andExpect(jsonPath("$.data.answers[0].text").value("모니터 받침대가 결합된 책상"))
                .andExpect(jsonPath("$.data.answers[1].text").value("데스크테리어 가능한 깔끔한 책상"));
    }

    @Test
    @DisplayName("다른 스타일 전체 조회 API는 STYLE 타입 목록을 반환한다")
    void getOtherStyles_returnsStyleList() throws Exception {
        Mockito.when(exploreService.getOtherStyles(2))
                .thenReturn(OtherStyleListResponse.of(List.of(
                        new OtherStyleResponse(1L, "미니멀한 개발자의 집", "https://google.com"),
                        new OtherStyleResponse(2L, "웜톤 우드 하우스", "https://google.com")
                )));

        mockMvc.perform(get("/api/v1/explore/other-styles")
                        .queryParam("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.otherStyles[0].id").value(1L))
                .andExpect(jsonPath("$.data.otherStyles[0].name").value("미니멀한 개발자의 집"))
                .andExpect(jsonPath("$.data.otherStyles[1].id").value(2L));
    }

    @Test
    @DisplayName("다른 스타일 전체 조회 API는 size가 없으면 전체 목록을 반환한다")
    void getOtherStyles_withoutSize_returnsAll() throws Exception {
        Mockito.when(exploreService.getOtherStyles(null))
                .thenReturn(OtherStyleListResponse.of(List.of(
                        new OtherStyleResponse(1L, "미니멀한 개발자의 집", "https://google.com"),
                        new OtherStyleResponse(2L, "웜톤 우드 하우스", "https://google.com"),
                        new OtherStyleResponse(3L, "하이틴 스튜디오", "https://google.com")
                )));

        mockMvc.perform(get("/api/v1/explore/other-styles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.otherStyles[0].id").value(1L))
                .andExpect(jsonPath("$.data.otherStyles[2].id").value(3L));
    }

    @Test
    @DisplayName("스타일 디테일 조회 API는 스타일 정보와 원본 상품 목록을 반환한다")
    void getOtherStyleDetail_returnsStyleDetail() throws Exception {
        Mockito.when(exploreService.getOtherStyleDetail(1L))
                .thenReturn(OtherStyleDetailResponse.of(
                        "미니멀한 개발자의 집",
                        "https://google.com/style",
                        "블랙을 중심으로 모노톤 인테리어 스타일",
                        List.of(
                                new OtherStyleDetailProductResponse(
                                        1L,
                                        "리샘 코지 저상형 평상형 무헤드 침대(SS/Q) 매트리스 선택",
                                        "https://google.com/product",
                                        39900L,
                                        30,
                                        27990L,
                                        "https://google.com/link"
                                )
                        )
                ));

        mockMvc.perform(get("/api/v1/explore/other-styles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.styleName").value("미니멀한 개발자의 집"))
                .andExpect(jsonPath("$.data.styleImageUrl").value("https://google.com/style"))
                .andExpect(jsonPath("$.data.styleDescription").value("블랙을 중심으로 모노톤 인테리어 스타일"))
                .andExpect(jsonPath("$.data.products[0].id").value(1L))
                .andExpect(jsonPath("$.data.products[0].name").value("리샘 코지 저상형 평상형 무헤드 침대(SS/Q) 매트리스 선택"))
                .andExpect(jsonPath("$.data.products[0].originalPrice").value(39900))
                .andExpect(jsonPath("$.data.products[0].discountRate").value(30))
                .andExpect(jsonPath("$.data.products[0].finalPrice").value(27990))
                .andExpect(jsonPath("$.data.products[0].linkUrl").value("https://google.com/link"));
    }

    @Test
    @DisplayName("최근 사용한 도면 조회 API는 최근 도면 데이터를 반환한다")
    void getRecentFloorPlan_returnsRecentFloorPlan() throws Exception {
        Mockito.when(exploreService.getRecentFloorPlan(Mockito.any(User.class)))
                .thenReturn(RecentFloorPlanResponse.withRecent(
                        RecentFloorPlanItemResponse.of(
                                or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan.builder()
                                        .id(1L)
                                        .floorPlanName("다용도실이 있는 원룸")
                                        .equilibrium(or.sopt.houme.domain.house.model.entity.enums.Equilibrium.BETWEEN_6_10)
                                        .build(),
                                "https://google.com/floor-plan",
                                "창가 뷰"
                        )
                ));
        CustomUserDetails customUserDetails = new CustomUserDetails(User.builder().id(1L).role(Role.ROLE_USER).build());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                customUserDetails.getAuthorities()
        );

        mockMvc.perform(get("/api/v1/explore/recent-floor-plan")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.hasRecentImage").value(true))
                .andExpect(jsonPath("$.data.floorPlan.id").value(1L))
                .andExpect(jsonPath("$.data.floorPlan.name").value("다용도실이 있는 원룸"))
                .andExpect(jsonPath("$.data.floorPlan.imageUrl").value("https://google.com/floor-plan"));
    }
}
