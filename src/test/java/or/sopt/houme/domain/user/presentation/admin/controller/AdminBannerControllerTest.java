package or.sopt.houme.domain.user.presentation.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerStyleAnswerChipRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerMappedRawProductResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerStyleAnswerChipResponse;
import or.sopt.houme.domain.user.service.admin.AdminBannerService;

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
class AdminBannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminBannerService adminBannerService;

    @Test
    @DisplayName("POST /api/v1/admin/banners 요청으로 배너를 생성할 수 있다")
    void createBanner_success() throws Exception {
        AdminBannerCreateRequest request = new AdminBannerCreateRequest(
                "https://image",
                "배너 제목",
                "배너 설명",
                "질문",
                "prompt",
                List.of(new AdminBannerStyleAnswerChipRequest(1, "칩", "선택 프롬프트", 1L)),
                List.of(1L)
        );
        when(adminBannerService.create(any(AdminBannerCreateRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.bannerTitle").value("배너 제목"))
                .andExpect(jsonPath("$.data.styleDescription").value("배너 설명"))
                .andExpect(jsonPath("$.data.styleAnswerChips[0].label").value("칩"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/banners 요청으로 배너 목록을 조회할 수 있다")
    void getBanners_success() throws Exception {
        when(adminBannerService.getAll()).thenReturn(new AdminBannerListResponse(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/admin/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.banners[0].id").value(1L))
                .andExpect(jsonPath("$.data.banners[0].mappedRawProducts[0].productName").value("책상 A"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/banners/{id} 요청으로 배너를 수정할 수 있다")
    void updateBanner_success() throws Exception {
        AdminBannerUpdateRequest request = new AdminBannerUpdateRequest(
                null,
                "수정 제목",
                "수정 설명",
                null,
                null,
                List.of(new AdminBannerStyleAnswerChipRequest(1, "새 칩", "새 선택 프롬프트", 2L)),
                List.of(2L)
        );
        AdminBannerResponse response = new AdminBannerResponse(
                1L,
                "https://image",
                "수정 제목",
                "수정 설명",
                "질문",
                "prompt",
                List.of(new AdminBannerStyleAnswerChipResponse(2L, 1, "새 칩", "새 선택 프롬프트", 2L, "책상 B", "https://image/2")),
                List.of(new AdminBannerMappedRawProductResponse(2L, "soozip", null, 22L, "책상 B", "https://image/2", "브랜드")),
                LocalDateTime.of(2026, 3, 13, 12, 0),
                LocalDateTime.of(2026, 3, 13, 12, 5)
        );
        when(adminBannerService.update(any(Long.class), any(AdminBannerUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/banners/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bannerTitle").value("수정 제목"))
                .andExpect(jsonPath("$.data.styleDescription").value("수정 설명"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/banners/image-upload-url 요청으로 presigned URL을 생성할 수 있다")
    void createBannerImageUploadUrl_success() throws Exception {
        AdminBannerImageUploadRequest request = new AdminBannerImageUploadRequest("png");
        when(adminBannerService.createImageUploadUrl(any(AdminBannerImageUploadRequest.class), any(String.class)))
                .thenReturn(new AdminBannerImageUploadResponse("https://upload-url", "https://public-url"));

        mockMvc.perform(post("/api/v1/admin/banners/image-upload-url")
                        .param("contentType", "image/png")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadUrl").value("https://upload-url"))
                .andExpect(jsonPath("$.data.publicUrl").value("https://public-url"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/banners/image-upload-url 요청에서 허용되지 않은 확장자는 400을 반환한다")
    void createBannerImageUploadUrl_invalidExtension() throws Exception {
        AdminBannerImageUploadRequest request = new AdminBannerImageUploadRequest("svg");

        mockMvc.perform(post("/api/v1/admin/banners/image-upload-url")
                        .param("contentType", "image/svg+xml")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/admin/banners/raw-products/search 요청으로 배너용 RAW 상품을 검색할 수 있다")
    void searchRawProducts_success() throws Exception {
        when(adminBannerService.searchRawProducts("책상", 10))
                .thenReturn(new AdminBannerRawProductSearchResponse(
                        List.of(new AdminBannerMappedRawProductResponse(1L, "soozip", null, 10L, "책상 A", "https://image/1", "브랜드"))
                ));

        mockMvc.perform(get("/api/v1/admin/banners/raw-products/search")
                        .param("keyword", "책상")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rawProducts[0].productName").value("책상 A"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/banners/{id} 요청으로 배너를 삭제할 수 있다")
    void deleteBanner_success() throws Exception {
        doNothing().when(adminBannerService).delete(1L);

        mockMvc.perform(delete("/api/v1/admin/banners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("배너가 삭제되었습니다."));
    }

    private AdminBannerResponse sampleResponse() {
        return new AdminBannerResponse(
                1L,
                "https://image",
                "배너 제목",
                "배너 설명",
                "질문",
                "prompt",
                List.of(new AdminBannerStyleAnswerChipResponse(1L, 1, "칩", "선택 프롬프트", 1L, "책상 A", "https://image/1")),
                List.of(new AdminBannerMappedRawProductResponse(1L, "soozip", null, 10L, "책상 A", "https://image/1", "브랜드")),
                LocalDateTime.of(2026, 3, 13, 12, 0),
                LocalDateTime.of(2026, 3, 13, 12, 5)
        );
    }
}
