package or.sopt.houme.domain.factor.presentation.controller;

import or.sopt.houme.domain.preference.presentation.controller.FactorController;
import or.sopt.houme.domain.preference.presentation.dto.response.FactorsResponse;
import or.sopt.houme.domain.preference.service.FactorService;
import or.sopt.houme.global.jwt.JWTFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FactorController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JWTFilter.class
        })
}, properties = {
        "external.image-api.base-url=http://localhost:8080",
        "gemini.api-base-url=https://generativelanguage.googleapis.com/v1beta"
})
@AutoConfigureMockMvc(addFilters = false)
class FactorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FactorService factorService;

    @Test
    @WithMockUser   // 인증된 사용자로 요청
    @DisplayName("GET /api/v1/factors - 요인 문구 조회 성공")
    void getFactors_success() throws Exception {
        // given
        FactorsResponse response = new FactorsResponse(
                List.of(
                        new FactorsResponse.FactorItem(1L, "인테리어 취향이 잘 반영됨"),
                        new FactorsResponse.FactorItem(2L, "가구가 잘 배치됨")
                )
        );

        Mockito.when(factorService.getFactors(true)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/factors")
                        .param("isLike", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.factors[0].text").value("인테리어 취향이 잘 반영됨"))
                .andExpect(jsonPath("$.data.factors[1].text").value("가구가 잘 배치됨"));
    }
}
