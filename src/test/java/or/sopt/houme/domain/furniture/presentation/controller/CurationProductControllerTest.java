package or.sopt.houme.domain.furniture.presentation.controller;

import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.service.CurationProductService;
import or.sopt.houme.global.jwt.JWTFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@DisplayName("상품 큐레이션 컨트롤러 테스트")
@WebMvcTest(controllers = CurationProductController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JWTFilter.class
        })
})
class CurationProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurationProductService curationProductService;

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/curations/products/filters - 필터링 조건 조회 API 호출 시 명세서 규격을 준수한다")
    void getFiltersSuccess() throws Exception {
        // given
        CurationProductFilterResponse mockResponse = new CurationProductFilterResponse(List.of(), List.of(), List.of());
        given(curationProductService.getFilterMetadata()).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/curations/products/filters"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.furnitureTypes").isArray())
                .andExpect(jsonPath("$.data.priceRanges").isArray())
                .andExpect(jsonPath("$.data.colors").isArray());
    }
}
