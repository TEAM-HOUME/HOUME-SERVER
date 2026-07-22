package or.sopt.houme.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("통합 테스트 인프라 검증 (Testcontainers PostgreSQL/Redis + 전체 컨텍스트)")
class IntegrationTestInfraVerificationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("전체 컨텍스트가 실제 PostgreSQL/Redis 위에서 기동된다")
    void contextLoadsOnRealInfra() {
        // IntegrationTestSupport 상속만으로 컨텍스트 기동이 검증된다
    }

    @Test
    @DisplayName("공개 API(/api/v1/landings)가 ApiResponse 포맷으로 200을 반환한다")
    void publicLandingsEndpointReturnsApiResponseFormat() throws Exception {
        mockMvc.perform(get("/api/v1/landings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"));
    }
}
