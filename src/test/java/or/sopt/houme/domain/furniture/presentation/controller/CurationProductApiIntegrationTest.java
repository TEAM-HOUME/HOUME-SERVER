package or.sopt.houme.domain.furniture.presentation.controller;

import or.sopt.houme.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 상품 큐레이션 API 계약 특성화 테스트 — 클러스터(furniture/curation) 헥사고날 전환(#582) 안전망.
 *
 * <p>{@code GET /api/v1/curations/products} 와 {@code /filters} 가 인증 없이(상품 화이트리스트)
 * ApiResponse 포맷으로 200을 반환하는 계약을 고정한다.
 */
class CurationProductApiIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("GET /api/v1/curations/products 는 인증 없이 ApiResponse 포맷으로 200을 반환한다")
    void getProducts_returnsApiResponse() throws Exception {
        mockMvc.perform(get("/api/v1/curations/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /api/v1/curations/products/filters 는 필터 메타데이터를 200으로 반환한다")
    void getFilters_returnsApiResponse() throws Exception {
        mockMvc.perform(get("/api/v1/curations/products/filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }
}
