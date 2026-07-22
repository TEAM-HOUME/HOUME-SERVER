package or.sopt.houme.domain.house.presentation.controller;

import or.sopt.houme.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 도면 템플릿(탐색) API 계약 특성화 테스트 — 클러스터(house/floorPlan) 헥사고날 전환(#582) 안전망.
 *
 * <p>{@code GET /api/v2/house-templates} 가 인증 없이(탐색 화이트리스트) ApiResponse 포맷으로
 * 200을 반환하는 계약을 고정한다. (데이터 미시드 시 빈 목록이라도 엔드포인트/엔벨로프 유지 검증)
 */
class HouseTemplatesApiIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("GET /api/v2/house-templates 는 인증 없이 ApiResponse 포맷으로 200을 반환한다")
    void exploreHouseTemplates_returnsApiResponse() throws Exception {
        mockMvc.perform(get("/api/v2/house-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").exists());
    }
}
