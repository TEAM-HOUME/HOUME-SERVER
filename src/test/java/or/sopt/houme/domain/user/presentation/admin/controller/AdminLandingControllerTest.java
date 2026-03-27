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
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request.AdminLandingCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request.AdminLandingUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response.AdminLandingListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response.AdminLandingResponse;
import or.sopt.houme.domain.user.service.admin.AdminLandingService;

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
class AdminLandingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminLandingService adminLandingService;

    @Test
    @DisplayName("POST /api/v1/admin/landings 요청으로 랜딩을 생성할 수 있다")
    void createLanding_success() throws Exception {
        AdminLandingCreateRequest request = new AdminLandingCreateRequest("https://landing-image", "랜딩 제목");
        when(adminLandingService.create(any(AdminLandingCreateRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/admin/landings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bannerImageUrl").value("https://landing-image"))
                .andExpect(jsonPath("$.data.bannerTitle").value("랜딩 제목"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/landings 요청으로 랜딩 목록을 조회할 수 있다")
    void getLandings_success() throws Exception {
        when(adminLandingService.getAll()).thenReturn(new AdminLandingListResponse(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/admin/landings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.landings[0].id").value(1L))
                .andExpect(jsonPath("$.data.landings[0].bannerImageUrl").value("https://landing-image"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/landings/{id} 요청으로 랜딩을 수정할 수 있다")
    void updateLanding_success() throws Exception {
        AdminLandingUpdateRequest request = new AdminLandingUpdateRequest("https://landing-image", "수정 랜딩 제목");
        AdminLandingResponse response = new AdminLandingResponse(
                1L,
                "https://landing-image",
                "수정 랜딩 제목",
                LocalDateTime.of(2026, 3, 27, 12, 0),
                LocalDateTime.of(2026, 3, 27, 12, 5)
        );
        when(adminLandingService.update(any(Long.class), any(AdminLandingUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/landings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bannerTitle").value("수정 랜딩 제목"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/landings/image-upload-url 요청으로 presigned URL을 생성할 수 있다")
    void createLandingImageUploadUrl_success() throws Exception {
        AdminBannerImageUploadRequest request = new AdminBannerImageUploadRequest("png");
        when(adminLandingService.createImageUploadUrl(any(AdminBannerImageUploadRequest.class), any(String.class)))
                .thenReturn(new AdminBannerImageUploadResponse("https://upload-url", "https://public-url"));

        mockMvc.perform(post("/api/v1/admin/landings/image-upload-url")
                        .param("contentType", "image/png")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadUrl").value("https://upload-url"))
                .andExpect(jsonPath("$.data.publicUrl").value("https://public-url"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/landings/{id} 요청으로 랜딩을 삭제할 수 있다")
    void deleteLanding_success() throws Exception {
        doNothing().when(adminLandingService).delete(1L);

        mockMvc.perform(delete("/api/v1/admin/landings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("랜딩이 삭제되었습니다."));
    }

    private AdminLandingResponse sampleResponse() {
        return new AdminLandingResponse(
                1L,
                "https://landing-image",
                "랜딩 제목",
                LocalDateTime.of(2026, 3, 27, 12, 0),
                LocalDateTime.of(2026, 3, 27, 12, 5)
        );
    }
}
