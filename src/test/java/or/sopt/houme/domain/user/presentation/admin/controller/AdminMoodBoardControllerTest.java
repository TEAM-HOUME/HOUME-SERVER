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
import or.sopt.houme.domain.user.presentation.admin.controller.dto.moodboard.AdminMoodBoardCreateRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.moodboard.AdminMoodBoardCreateResponseDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.moodboard.AdminMoodBoardGetAllResponseDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.moodboard.AdminMoodBoardGetResponseDTO;
import or.sopt.houme.domain.user.service.admin.AdminMoodBoardService;

import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminMoodBoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminMoodBoardService adminMoodBoardService;


    @Test
    @DisplayName("POST /api/v1/admin/moodboard 요청으로 무드보드를 생성할 수 있다")
    void createMoodBoard_success() throws Exception {
        // given
        AdminMoodBoardCreateRequestDTO requestDTO = new AdminMoodBoardCreateRequestDTO("jpg", "image.jpg", 1L);
        AdminMoodBoardCreateResponseDTO responseDTO = new AdminMoodBoardCreateResponseDTO("uploadUrl", 1L);

        when(adminMoodBoardService.create(any(AdminMoodBoardCreateRequestDTO.class), eq(MediaType.IMAGE_JPEG_VALUE))).thenReturn(responseDTO);

        // when & then
        mockMvc.perform(post("/api/v1/admin/moodboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .param("contentType", MediaType.IMAGE_JPEG_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.presignedUrl").value("uploadUrl"))
                .andExpect(jsonPath("$.data.tasteId").value(1L));
    }


    @Test
    @DisplayName("GET /api/v1/admin/moodboards 요청으로 모든 무드보드를 조회할 수 있다")
    void getAllMoodBoards_success() throws Exception {
        // given
        AdminMoodBoardGetResponseDTO moodBoard1 = new AdminMoodBoardGetResponseDTO("file1", "orig1", "url1");
        AdminMoodBoardGetResponseDTO moodBoard2 = new AdminMoodBoardGetResponseDTO("file2", "orig2", "url2");
        AdminMoodBoardGetAllResponseDTO responseDTO = new AdminMoodBoardGetAllResponseDTO(List.of(moodBoard1, moodBoard2));

        when(adminMoodBoardService.getAll()).thenReturn(responseDTO);

        // when & then
        mockMvc.perform(get("/api/v1/admin/moodboards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.dtos[0].filename").value("file1"))
                .andExpect(jsonPath("$.data.dtos[1].filename").value("file2"));
    }


    @Test
    @DisplayName("DELETE /api/v1/admin/moodboard 요청으로 무드보드를 삭제할 수 있다")
    void deleteMoodBoard_success() throws Exception {
        // given
        String filename = "test.jpg";
        doNothing().when(adminMoodBoardService).delete(filename);

        // when & then
        mockMvc.perform(delete("/api/v1/admin/moodboard")
                        .param("filename", filename))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("무드보드 삭제에 성공하였습니다"));
    }
}
