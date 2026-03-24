package or.sopt.houme.domain.user.presentation.controller;

import or.sopt.houme.domain.user.presentation.controller.dto.LandingListResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.LandingResponse;
import or.sopt.houme.domain.user.service.UserLandingService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserLandingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserLandingService userLandingService;

    @Test
    @DisplayName("랜딩 페이지 전체 조회 API는 랜딩 목록을 반환한다")
    void getLandings_returnsLandingList() throws Exception {
        Mockito.when(userLandingService.getLandings())
                .thenReturn(LandingListResponse.of(List.of(
                        new LandingResponse(1L, "재택근무가 필요한", "https://google.com"),
                        new LandingResponse(2L, "취향 탐색이 필요한", "https://google.com")
                )));

        mockMvc.perform(get("/api/v1/landings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.landings[0].id").value(1L))
                .andExpect(jsonPath("$.data.landings[0].name").value("재택근무가 필요한"))
                .andExpect(jsonPath("$.data.landings[0].imageUrl").value("https://google.com"))
                .andExpect(jsonPath("$.data.landings[1].id").value(2L));
    }

    @Test
    @DisplayName("checkHasGeneratedImage()는 true를 반환한다")
    void checkHasGeneratedImage_true() throws Exception {
        // given
        Mockito.when(userLandingService.getHasGeneratedImage(any()))
                .thenReturn(true);

        // when & then
        mockMvc.perform(get("/api/v1/check-has-generated-image")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }


    @Test
    @DisplayName("checkHasGeneratedImage()는 false를 반환한다")
    void checkHasGeneratedImage_false() throws Exception {
        // given
        Mockito.when(userLandingService.getHasGeneratedImage(any()))
                .thenReturn(false);

        // when & then
        mockMvc.perform(get("/api/v1/check-has-generated-image")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }
}
