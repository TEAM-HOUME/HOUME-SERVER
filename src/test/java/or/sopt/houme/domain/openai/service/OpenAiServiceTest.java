package or.sopt.houme.domain.openai.service;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import feign.FeignException;
import or.sopt.houme.domain.openai.client.OpenAIImageClient;
import or.sopt.houme.domain.openai.controller.dto.OpenAiRequest;
import or.sopt.houme.domain.openai.controller.dto.OpenAiResponse;
import or.sopt.houme.domain.openai.controller.dto.OpenAiResponse.ImageData;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.ChatGptException;
import or.sopt.houme.global.api.handler.S3Exception;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.S3Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("OpenAiService Test")
class OpenAiServiceTest {

    @Autowired
    private OpenAiService openAiService;

    @MockBean
    private OpenAIImageClient openAIImageClient;

    @MockBean
    private S3Util s3Util;

    private byte[] imageBytes;
    private String base64Image;


    @BeforeEach
    void setUp() {
        imageBytes = "mock image".getBytes();
        base64Image = Base64.getEncoder().encodeToString(imageBytes);
    }


    @Test
    @DisplayName("createImage() 를 통해 생성형 이미지를 생성 하고 S3에 저장 할 수 있다")
    void createImage_success() {

        // given
        ImageData imageData = new ImageData();
        imageData.setB64_json(base64Image);
        OpenAiResponse response = new OpenAiResponse(Collections.singletonList(imageData));

        when(openAIImageClient.generateImage(anyString(), any(OpenAiRequest.class))).thenReturn(response);
        when(s3Util.uploadByByte(anyString(), eq(imageBytes)))
                .thenReturn(ImageUploadResponseDTO.from("filename", "original.png", "http://mock.url"));

        // when
        ImageUploadResponseDTO result = openAiService.createImage("tree in the desert");

        // then
        assertNotNull(result);
        assertEquals("http://mock.url", result.getImageLink());
        verify(openAIImageClient).generateImage(anyString(), any(OpenAiRequest.class));
        verify(s3Util).uploadByByte(anyString(), eq(imageBytes));
    }


    @Test
    @DisplayName("generateImage() 를 통해 byte[] 타입의 이미지를 반환 할 수 있다")
    void getGptImage_ShouldReturnDecodedImage_WhenValidBase64() {
        // given
        byte[] imageBytes = "hello-image".getBytes();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        ImageData imageData = new ImageData();
        imageData.setB64_json(base64);
        OpenAiResponse response = new OpenAiResponse(Collections.singletonList(imageData));
        response.setData(Collections.singletonList(imageData));

        when(openAIImageClient.generateImage(anyString(), any(OpenAiRequest.class))).thenReturn(response);

        // when
        byte[] result = openAiService.getGptImage(OpenAiRequest.of("tree"));

        // then
        assertArrayEquals(imageBytes, result);
    }


    @Test
    @DisplayName("generateImage() 중 FeignException 이 발생하면 정해진 예외가 발생한다")
    void createImage_feignException() {

        // given & when
        when(openAIImageClient.generateImage(anyString(), any(OpenAiRequest.class)))
                .thenThrow(mock(FeignException.class));

        // then
        assertThrows(ChatGptException.class, () -> openAiService.createImage("prompt"));
    }


    @Test
    @DisplayName("generateImage() 의 결과로 null 값이 반환되면 정해진 예외가 발생한다")
    void createImage_emptyData() {
        // given & when
        when(openAIImageClient.generateImage(anyString(), any(OpenAiRequest.class)))
                .thenReturn(new OpenAiResponse(Collections.emptyList()));

        assertThrows(ChatGptException.class, () -> openAiService.createImage("prompt"));
    }


    @Test
    @DisplayName("generateImage() 중 인코딩에 실패하면 정해진 예외가 발생한다")
    void createImage_invalidBase64() {
        // given
        ImageData imageData = new ImageData();
        imageData.setB64_json("not_base64");
        OpenAiResponse response = new OpenAiResponse(Collections.singletonList(imageData));

        // when
        when(openAIImageClient.generateImage(anyString(), any(OpenAiRequest.class))).thenReturn(response);

        // then
        S3Exception exception = assertThrows(S3Exception.class, () -> openAiService.createImage("prompt"));
        assertEquals(ErrorCode.INCODING_EXCEPTION, exception.getErrorCode());
    }
}
