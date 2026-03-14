package or.sopt.houme.domain.user.presentation.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerMappedRawProductResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request.AdminStyleCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request.AdminStyleUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response.AdminStyleListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response.AdminStyleResponse;
import or.sopt.houme.domain.user.service.admin.AdminStyleService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminStyleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminStyleService adminStyleService;

    @Test
    @DisplayName("POST /api/v1/admin/styles 요청으로 스타일을 생성할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void createStyle_success() throws Exception {
        AdminStyleCreateRequest request = new AdminStyleCreateRequest(
                "https://image",
                "스타일 제목",
                "스타일 설명",
                "prompt",
                List.of(1L)
        );
        when(adminStyleService.create(any(AdminStyleCreateRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/admin/styles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bannerTitle").value("스타일 제목"))
                .andExpect(jsonPath("$.data.styleDescription").value("스타일 설명"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/styles 요청으로 스타일 목록을 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getStyles_success() throws Exception {
        when(adminStyleService.getAll()).thenReturn(new AdminStyleListResponse(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/admin/styles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.styles[0].id").value(1L))
                .andExpect(jsonPath("$.data.styles[0].mappedRawProducts[0].productName").value("책상 A"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/styles/{id} 요청으로 스타일을 수정할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void updateStyle_success() throws Exception {
        AdminStyleUpdateRequest request = new AdminStyleUpdateRequest(
                null,
                "수정 스타일",
                "수정 설명",
                "new prompt",
                List.of(2L)
        );
        AdminStyleResponse response = new AdminStyleResponse(
                1L,
                "https://image",
                "수정 스타일",
                "수정 설명",
                "new prompt",
                List.of(new AdminBannerMappedRawProductResponse(2L, "soozip", null, 22L, "책상 B", "https://image/2", "브랜드")),
                LocalDateTime.of(2026, 3, 13, 12, 0),
                LocalDateTime.of(2026, 3, 13, 12, 5)
        );
        when(adminStyleService.update(any(Long.class), any(AdminStyleUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/styles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bannerTitle").value("수정 스타일"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/styles/image-upload-url 요청으로 presigned URL을 생성할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void createStyleImageUploadUrl_success() throws Exception {
        AdminBannerImageUploadRequest request = new AdminBannerImageUploadRequest("png");
        when(adminStyleService.createImageUploadUrl(any(AdminBannerImageUploadRequest.class), any(String.class)))
                .thenReturn(new AdminBannerImageUploadResponse("https://upload-url", "https://public-url"));

        mockMvc.perform(post("/api/v1/admin/styles/image-upload-url")
                        .param("contentType", "image/png")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadUrl").value("https://upload-url"))
                .andExpect(jsonPath("$.data.publicUrl").value("https://public-url"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/styles/image-upload-url 요청에서 허용되지 않은 확장자는 400을 반환한다")
    @WithMockUser(roles = "ADMIN")
    void createStyleImageUploadUrl_invalidExtension() throws Exception {
        AdminBannerImageUploadRequest request = new AdminBannerImageUploadRequest("svg");

        mockMvc.perform(post("/api/v1/admin/styles/image-upload-url")
                        .param("contentType", "image/svg+xml")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/admin/styles/raw-products/search 요청으로 스타일용 RAW 상품을 검색할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void searchRawProducts_success() throws Exception {
        when(adminStyleService.searchRawProducts("책상", 10))
                .thenReturn(new AdminBannerRawProductSearchResponse(
                        List.of(new AdminBannerMappedRawProductResponse(1L, "soozip", null, 10L, "책상 A", "https://image/1", "브랜드"))
                ));

        mockMvc.perform(get("/api/v1/admin/styles/raw-products/search")
                        .param("keyword", "책상")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rawProducts[0].productName").value("책상 A"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/styles/{id} 요청으로 스타일을 삭제할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void deleteStyle_success() throws Exception {
        doNothing().when(adminStyleService).delete(1L);

        mockMvc.perform(delete("/api/v1/admin/styles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("스타일이 삭제되었습니다."));
    }

    private AdminStyleResponse sampleResponse() {
        return new AdminStyleResponse(
                1L,
                "https://image",
                "스타일 제목",
                "스타일 설명",
                "prompt",
                List.of(new AdminBannerMappedRawProductResponse(1L, "soozip", null, 10L, "책상 A", "https://image/1", "브랜드")),
                LocalDateTime.of(2026, 3, 13, 12, 0),
                LocalDateTime.of(2026, 3, 13, 12, 5)
        );
    }
}
