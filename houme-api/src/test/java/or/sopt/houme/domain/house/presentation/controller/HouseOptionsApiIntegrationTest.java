package or.sopt.houme.domain.house.presentation.controller;

import or.sopt.houme.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 집 구조 옵션 API 계약 특성화 테스트 — 클러스터(house) 헥사고날 전환(#582) 안전망.
 *
 * <p>{@code GET /api/v1/housing-options} 가 주거형태/공간구조/평형 3개 옵션 목록을
 * ApiResponse 포맷으로 반환하는 현재 계약을 고정한다. (인증 불필요, 정적 enum 기반)
 */
class HouseOptionsApiIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("GET /api/v1/housing-options 는 주거형태/공간구조/평형 옵션 목록을 반환한다")
    void housingOptions_returnsThreeOptionGroups() throws Exception {
        mockMvc.perform(get("/api/v1/housing-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.houseTypes.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.roomTypes.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.areaTypes.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.houseTypes[0].code").exists())
                .andExpect(jsonPath("$.data.houseTypes[0].label").exists());
    }
}
