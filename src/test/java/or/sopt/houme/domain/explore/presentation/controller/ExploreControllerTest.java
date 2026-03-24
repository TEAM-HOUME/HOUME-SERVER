package or.sopt.houme.domain.explore.presentation.controller;

import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreResponse;
import or.sopt.houme.domain.explore.service.ExploreService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
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
}
