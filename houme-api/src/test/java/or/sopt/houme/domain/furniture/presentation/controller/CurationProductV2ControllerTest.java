package or.sopt.houme.domain.furniture.presentation.controller;

import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductMetaResponse;
import or.sopt.houme.domain.furniture.service.CurationProductService;
import or.sopt.houme.global.jwt.JWTFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import or.sopt.houme.global.api.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@DisplayName("상품 큐레이션 v2 컨트롤러 테스트")
@Import(ValidationAutoConfiguration.class)
@WebMvcTest(controllers = CurationProductV2Controller.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JWTFilter.class
        })
})
class CurationProductV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurationProductService curationProductService;

    @Test
    @WithMockUser
    @DisplayName("GET /api/v2/curations/products - 키워드 없이 호출 시 200 응답과 명세서 규격을 준수한다")
    void getProducts_withoutKeyword() throws Exception {
        // given
        CurationProductListResponse mockResponse = new CurationProductListResponse(
                List.of(),
                new CurationProductMetaResponse(null, false, List.of(), false)
        );
        given(curationProductService.getProductsV2(any(), any(), any(), any(), any(), any()))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v2/curations/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.products").isArray())
                .andExpect(jsonPath("$.data.meta.hasNext").value(false))
                .andExpect(jsonPath("$.data.meta.appliedFilters").isArray());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v2/curations/products - 키워드 검색 시 서비스에 keyword 파라미터가 전달된다")
    void getProducts_withKeyword() throws Exception {
        // given
        CurationProductListResponse mockResponse = new CurationProductListResponse(
                List.of(),
                new CurationProductMetaResponse(null, false, List.of(), false)
        );
        given(curationProductService.getProductsV2(eq("매트리스"), any(), any(), any(), any(), any()))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v2/curations/products").param("keyword", "매트리스"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v2/curations/products - size가 100을 초과하면 400을 반환한다")
    void getProducts_invalidSizeReturns400() throws Exception {
        mockMvc.perform(get("/api/v2/curations/products").param("size", "101"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
