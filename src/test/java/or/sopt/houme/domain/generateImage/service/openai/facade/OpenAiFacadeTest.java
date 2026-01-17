package or.sopt.houme.domain.generateImage.service.openai.facade;

import or.sopt.houme.domain.generateImage.infrastructure.openai.service.OpenAiService;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.generateImage.service.prompt.PromptService;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAiFacadeImpl Test")
class OpenAiFacadeTest {

    @Mock
    private OpenAiService openAiService;

    @Mock
    private PromptService promptService;

    @InjectMocks
    private OpenAiFacadeImpl openAiFacade;

    @Test
    @DisplayName("makeImage() 를 통해서 프롬프트를 활용해서 이미지를 생성할 수 있다")
    void makeImage_success() {
        // Given
        PromptFurnitureListDTO furnitureListDTO = PromptFurnitureListDTO.of(List.of(1L, 2L, 3L));
        PromptRequestDTO requestDTO = PromptRequestDTO.of(
                1L,
                2L,
                Equilibrium.UNDER_5,
                furnitureListDTO
        );

        String generatedPrompt = "test prompt";

        ImageUploadResponseDTO mockResponse = ImageUploadResponseDTO.from(
                "file-key",
                "original.png",
                "https://s3.aws.com/file-key",
                "jpg"
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