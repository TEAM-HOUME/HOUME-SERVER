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
import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminTagUpdateRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagDeleteRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagGetAllResponseDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagGetResponseDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagRequestDTO;
import or.sopt.houme.domain.user.service.admin.AdminTagService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminTagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminTagService adminTagService;

    @Test
    @DisplayName("POST /api/v1/admin/tag 요청으로 태그를 생성할 수 있다")
    void createTag_success() throws Exception {
        // given
        AdminTagRequestDTO requestDTO = new AdminTagRequestDTO(1, "minimal", "미니멀", "a minimal mood");

        // when & then
        mockMvc.perform(post("/api/v1/admin/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("성공적으로 스타일 태그가 생성 되었습니다"));
    }


    @Test
    @DisplayName("GET /api/v1/admin/tags 요청으로 모든 태그를 조회할 수 있다")
    void getTags_success() throws Exception {
        // given
        AdminTagGetResponseDTO tag1 = new AdminTagGetResponseDTO(1L, 1, "minimal", "미니멀", "a minimal mood");
        AdminTagGetResponseDTO tag2 = new AdminTagGetResponseDTO(2L, 2, "modern", "모던", "a modern mood");
        AdminTagGetAllResponseDTO responseDTO = new AdminTagGetAllResponseDTO(List.of(tag1, tag2));

        when(adminTagService.getAll()).thenReturn(responseDTO);

        // when & then
        mockMvc.perform(get("/api/v1/admin/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.tagGetResponseDTOS[0].tagName").value("minimal"))
                .andExpect(jsonPath("$.data.tagGetResponseDTOS[1].tagName").value("modern"));
    }


    @Test
    @DisplayName("PATCH /api/v1/admin/tag 요청으로 태그를 업데이트할 수 있다")
    void updateTag_success() throws Exception {
        // given
        AdminTagUpdateRequestDTO requestDTO = new AdminTagUpdateRequestDTO(1L, 2, "new minimal", "a new minimal mood", null);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("성공적으로 스타일 태그가 업데이트 되었습니다"));
    }


    @Test
    @DisplayName("DELETE /api/v1/admin/tag 요청으로 태그를 삭제할 수 있다")
    void deleteTag_success() throws Exception {
        // given
        AdminTagDeleteRequestDTO requestDTO = new AdminTagDeleteRequestDTO(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/admin/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("성공적으로 스타일 태그가 삭제 되었습니다"));
    }
}
