package or.sopt.houme.domain.openai.facade;

import or.sopt.houme.domain.openai.service.OpenAiService;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.prompt.service.PromptService;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAiFacade Test")
class OpenAiFacadeTest {

    @Mock
    private OpenAiService openAiService;

    @Mock
    private PromptService promptService;

    @InjectMocks
    private OpenAiFacade openAiFacade;


    @Test
    @DisplayName("makeImage() 를 통해서 프롬프트를 활용해서 이미지를 생성할 수 있다")
    void makeImage_success() {
        // Given
        PromptRequestDTO requestDTO = new PromptRequestDTO();
        String generatedPrompt = "test prompt";
        ImageUploadResponseDTO mockResponse = ImageUploadResponseDTO.from(
                "file-key",
                "original.png",
                "https://s3.aws.com/file-key"
        );

        when(promptService.makePrompt(requestDTO)).thenReturn(generatedPrompt);
        when(openAiService.createImage(generatedPrompt)).thenReturn(mockResponse);

        // When
        ImageUploadResponseDTO result = openAiFacade.makeImage(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals("file-key", result.getFilename());
        assertEquals("original.png", result.getOriginalFilename());
        assertEquals("https://s3.aws.com/file-key", result.getImageLink());

        verify(promptService).makePrompt(requestDTO);
        verify(openAiService).createImage(generatedPrompt);
    }
}
