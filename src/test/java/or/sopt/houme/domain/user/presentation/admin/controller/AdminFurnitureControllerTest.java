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
import or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.*;
import or.sopt.houme.domain.user.service.admin.AdminFurnitureService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminFurnitureControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminFurnitureService adminFurnitureService;


    @Test
    @DisplayName("POST /api/v1/admin/furniture 요청으로 가구를 등록할 수 있다")
    void registerFurniture_success() throws Exception {
        // given
        AdminFurnitureRequestDTO requestDTO = new AdminFurnitureRequestDTO("테스트 가구", "test furniture", 1L);

        // when & then
        mockMvc.perform(post("/api/v1/admin/furniture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("가구가 등록되었습니다"));
    }


    @Test
    @DisplayName("POST /api/v1/admin/furniture/prompt 요청으로 가구 프롬프트를 등록할 수 있다 (presigned URL 반환)")
    void registerFurniturePrompt_success() throws Exception {
        // given
        AdminFurniturePromptRequestDTO requestDTO = new AdminFurniturePromptRequestDTO(
                "테스트 가구",
                "테스트 프롬프트",
                1L,
                "키워드",
                0,
                "jpg",
                "원본파일명"
        );

        when(adminFurnitureService.registerFurniturePrompt(any(AdminFurniturePromptRequestDTO.class), any(String.class)))
                .thenReturn(new AdminFurniturePromptCreateResponseDTO("http://example.com/presigned", 10L));

        // when & then
        mockMvc.perform(post("/api/v1/admin/furniture/prompt")
                        .param("contentType", "image/jpeg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.presignedUrl").value("http://example.com/presigned"))
                .andExpect(jsonPath("$.data.furnitureTagId").value(10));
    }


    @Test
    @DisplayName("GET /api/v1/admin/furnitures 요청으로 모든 가구를 조회할 수 있다")
    void getFurnitures_success() throws Exception {
        // given
        AdminFurnitureGetDTO.TagInfo tagInfo = new AdminFurnitureGetDTO.TagInfo(
                100L, // furnitureTagId
                1L,   // tagId
                "모던",
                "http://img",
                "키워드",
                0
        );
        AdminFurnitureGetDTO.FurnitureInfo furnitureInfo = new AdminFurnitureGetDTO.FurnitureInfo(1L, "침대", List.of(tagInfo));
        AdminFurnitureGetDTO responseDTO = new AdminFurnitureGetDTO(List.of(furnitureInfo));

        when(adminFurnitureService.getFurniture()).thenReturn(responseDTO);

        // when & then
        mockMvc.perform(get("/api/v1/admin/furnitures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.furnitures[0].furnitureId").value(1L))
                .andExpect(jsonPath("$.data.furnitures[0].furnitureNameKr").value("침대"))
                .andExpect(jsonPath("$.data.furnitures[0].tags[0].tagId").value(1L))
                .andExpect(jsonPath("$.data.furnitures[0].tags[0].tagName").value("모던"))
                .andExpect(jsonPath("$.data.furnitures[0].tags[0].imageUrl").value("http://img"));
    }


    @Test
    @DisplayName("GET /api/v1/admin/furniture/tags 요청으로 모든 가구 태그를 조회할 수 있다")
    void getFurnitureTags_success() throws Exception {
        // given
        AdminFurnitureTagGetDTO responseDTO = new AdminFurnitureTagGetDTO(List.of(1L, 2L), List.of("모던", "미니멀"));

        when(adminFurnitureService.getFurnitureTag()).thenReturn(responseDTO);

        // when & then
        mockMvc.perform(get("/api/v1/admin/furniture/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.tagId[0]").value(1L))
                .andExpect(jsonPath("$.data.tagId[1]").value(2L))
                .andExpect(jsonPath("$.data.tagNameKr[0]").value("모던"))
                .andExpect(jsonPath("$.data.tagNameKr[1]").value("미니멀"));
    }


    @Test
    @DisplayName("PATCH /api/v1/admin/furniture 요청으로 가구 정보를 수정할 수 있다 (presigned URL 옵션)")
    void updateFurniture_success() throws Exception {
        // given
        AdminFurnitureUpdateRequestDTO requestDTO = new AdminFurnitureUpdateRequestDTO(
                "침대",
                1L,
                "new bed eng",
                "new prompt",
                null,
                null,
                null,
                null
        );

        when(adminFurnitureService.updateFurniture(any(AdminFurnitureUpdateRequestDTO.class), any()))
                .thenReturn(AdminFurnitureUpdateResponseDTO.of(null, 1L));

        // when & then
        mockMvc.perform(patch("/api/v1/admin/furniture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.furnitureTagId").value(1));
    }


    @Test
    @DisplayName("DELETE /api/v1/admin/furniture/tag 요청으로 가구 태그를 삭제할 수 있다")
    void deleteFurnitureTag_success() throws Exception {
        // given
        AdminFurnitureTagDeleteDTO requestDTO = new AdminFurnitureTagDeleteDTO("침대", 1L);

        // when & then
        mockMvc.perform(delete("/api/v1/admin/furniture/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("삭제가 성공적으로 완료되었습니다"));
    }


    @Test
    @DisplayName("DELETE /api/v1/admin/furniture 요청으로 가구를 삭제할 수 있다")
    void deleteFurniture_success() throws Exception {
        // given
        AdminFurnitureDeleteDTO requestDTO = new AdminFurnitureDeleteDTO("침대");

        // when & then
        mockMvc.perform(delete("/api/v1/admin/furniture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("삭제가 성공적으로 완료되었습니다"));
    }


    @Test
    @DisplayName("GET /api/v1/admin/furniture/prompt 요청으로 가구 프롬프트를 조회할 수 있다")
    void getFurniturePrompt_success() throws Exception {
        // given
        AdminFurnitureDetailsResponseDTO responseDTO = new AdminFurnitureDetailsResponseDTO("테스트 프롬프트");
        when(adminFurnitureService.getDetails(any(AdminFurnitureDetailsRequestDTO.class))).thenReturn(responseDTO);

        // when & then
        mockMvc.perform(get("/api/v1/admin/furniture/prompt")
                        .param("furnitureNameKr", "침대")
                        .param("tagId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.prompt").value("테스트 프롬프트"));
    }
}
