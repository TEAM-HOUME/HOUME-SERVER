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
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanImageResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanResponse;
import or.sopt.houme.domain.user.service.admin.AdminFloorPlanService;

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
class AdminFloorPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminFloorPlanService adminFloorPlanService;

    @Test
    @DisplayName("POST /api/v1/admin/floor-plans 요청으로 도면을 생성할 수 있다")
    void createFloorPlan_success() throws Exception {
        AdminFloorPlanCreateRequest request = new AdminFloorPlanCreateRequest(
                Form.OFFICETEL,
                Structure.OPEN_ONE_ROOM,
                Equilibrium.UNDER_5,
                "도면 프롬프트",
                List.of(new AdminFloorPlanImageRequest("https://image/1", "fp-1.png", "room-1.png", "png", 1))
        );

        when(adminFloorPlanService.create(any(AdminFloorPlanCreateRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/admin/floor-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.form").value("OFFICETEL"))
                .andExpect(jsonPath("$.data.images[0].url").value("https://image/1"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/floor-plans 요청으로 도면 목록을 조회할 수 있다")
    void getFloorPlans_success() throws Exception {
        when(adminFloorPlanService.getAll()).thenReturn(new AdminFloorPlanListResponse(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/admin/floor-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.floorPlans[0].id").value(1L))
                .andExpect(jsonPath("$.data.floorPlans[0].equilibrium").value("UNDER_5"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/floor-plans/{id} 요청으로 도면을 수정할 수 있다")
    void updateFloorPlan_success() throws Exception {
        AdminFloorPlanUpdateRequest request = new AdminFloorPlanUpdateRequest(
                Form.APARTMENT,
                Structure.TWO_ROOM,
                Equilibrium.OVER_16,
                "수정 프롬프트",
                List.of(new AdminFloorPlanImageRequest("https://image/2", "fp-2.png", "room-2.png", "png", 1))
        );
        AdminFloorPlanResponse response = new AdminFloorPlanResponse(
                1L,
                Form.APARTMENT,
                Structure.TWO_ROOM,
                Equilibrium.OVER_16,
                "수정 프롬프트",
                "https://image/2",
                List.of(new AdminFloorPlanImageResponse("https://image/2", "fp-2.png", "room-2.png", "png", 1))
        );

        when(adminFloorPlanService.update(any(Long.class), any(AdminFloorPlanUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/floor-plans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.structure").value("TWO_ROOM"))
                .andExpect(jsonPath("$.data.representativeImageUrl").value("https://image/2"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/floor-plans/image-upload-url 요청으로 업로드 URL을 생성할 수 있다")
    void createFloorPlanImageUploadUrl_success() throws Exception {
        when(adminFloorPlanService.createImageUploadUrl(any(AdminFloorPlanImageUploadRequest.class), any(String.class)))
                .thenReturn(new AdminFloorPlanImageUploadResponse("https://upload-url", "https://public-url"));

        mockMvc.perform(post("/api/v1/admin/floor-plans/image-upload-url")
                        .param("contentType", "image/png")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminFloorPlanImageUploadRequest("png"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadUrl").value("https://upload-url"))
                .andExpect(jsonPath("$.data.publicUrl").value("https://public-url"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/floor-plans/{id} 요청으로 도면을 삭제할 수 있다")
    void deleteFloorPlan_success() throws Exception {
        doNothing().when(adminFloorPlanService).delete(1L);

        mockMvc.perform(delete("/api/v1/admin/floor-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("도면이 삭제되었습니다."));
    }

    private AdminFloorPlanResponse sampleResponse() {
        return new AdminFloorPlanResponse(
                1L,
                Form.OFFICETEL,
                Structure.OPEN_ONE_ROOM,
                Equilibrium.UNDER_5,
                "도면 프롬프트",
                "https://image/1",
                List.of(new AdminFloorPlanImageResponse("https://image/1", "fp-1.png", "room-1.png", "png", 1))
        );
    }
}
