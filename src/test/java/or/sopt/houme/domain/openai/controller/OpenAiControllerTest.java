package or.sopt.houme.domain.openai.controller;

import or.sopt.houme.domain.openai.facade.OpenAiFacadeImpl;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class OpenAiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenAiFacadeImpl openAiFacade;

    @Test
    @WithMockUser(username = "test12", roles = "USER")
    @DisplayName("/image/generate 를 통해 생성된 이미지를 전송 할 수 있다")
    void testGenerateImage_success() throws Exception {
        // given
        ImageUploadResponseDTO mockResponse = ImageUploadResponseDTO.from(
                "generated_123.png",
                "original_prompt.png",
                "https://example.com/generated_123.png",
                "jpg"
        );

        Mockito.when(openAiFacade.testMakeImage())
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/image/generate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("https://example.com/generated_123.png"))
                .andExpect(jsonPath("$.data").value("https://example.com/generated_123.png"));
    }
}
