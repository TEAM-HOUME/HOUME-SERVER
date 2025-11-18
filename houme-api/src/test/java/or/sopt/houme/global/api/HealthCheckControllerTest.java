package or.sopt.houme.global.api;

import or.sopt.houme.global.jwt.JWTFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@DisplayName("테스트 환경 Health Check")
@WebMvcTest(controllers = HealthCheckController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JWTFilter.class
        })
})
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser   // 인증된 사용자로 요청
    @DisplayName("/env API가 현재 배포 환경(server.env)을 반환한다")
    void getHealthCheckEnv() throws Exception {
        mockMvc.perform(get("/api/v1/env"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.env").value("test"));
    }
}