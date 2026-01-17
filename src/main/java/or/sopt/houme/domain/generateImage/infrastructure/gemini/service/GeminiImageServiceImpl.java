package or.sopt.houme.domain.generateImage.infrastructure.gemini.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.client.GeminiImageClient;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.dto.GeminiImageRequest;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.dto.GeminiImageResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.ChatGptException;
import or.sopt.houme.global.api.handler.S3Exception;
import or.sopt.houme.global.config.GeminiImageConfig;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.S3Util;
import or.sopt.houme.global.util.constant.S3Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiImageServiceImpl implements GeminiImageService {

    private final GeminiImageClient geminiImageClient;
    private final GeminiImageConfig geminiImageConfig;
    private final S3Util s3Util;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Override
    public ImageUploadResponseDTO createImage(String prompt) {
        // Gemini는 해상도 파라미터를 받지 않으므로 프롬프트 힌트로만 전달합니다.
        String promptWithSize = applySizeHint(prompt, geminiImageConfig.getSize());
        GeminiImageRequest request = GeminiImageRequest.of(promptWithSize);

        try {
            GeminiImageResponse response = geminiImageClient.generateImage(
                    defaultModel(geminiImageConfig.getModel()),
                    apiKey,
                    request
            );

            // Gemini는 inlineData에 base64 문자열로 이미지를 반환합니다.
            byte[] image = decodeBase64(extractBase64(response));
            ImageUploadResponseDTO responseDTO = s3Util.uploadByByte(S3Constant.CHAT_GPT_DIRNAME, image);
            responseDTO.setPullPrompt(promptWithSize);
            return responseDTO;
        } catch (FeignException e) {
            log.info(e.getMessage());
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        } catch (IllegalArgumentException e) {
            throw new S3Exception(ErrorCode.INCODING_EXCEPTION);
        }
    }

    private String extractBase64(GeminiImageResponse response) {
        // 후보 목록에서 첫 번째 이미지(base64)를 찾아 반환합니다.
        if (response == null || response.candidates() == null) {
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        }

        for (GeminiImageResponse.Candidate candidate : response.candidates()) {
            if (candidate == null || candidate.content() == null || candidate.content().parts() == null) {
                continue;
            }
            for (GeminiImageResponse.Part part : candidate.content().parts()) {
                if (part != null && part.inlineData() != null && part.inlineData().data() != null) {
                    return part.inlineData().data();
                }
            }
        }

        throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
    }

    private byte[] decodeBase64(String base64) {
        // base64 문자열을 실제 바이너리 이미지로 변환합니다.
        return Base64.getDecoder().decode(base64);
    }

    private String defaultModel(String model) {
        // 설정이 비어있으면 기본 Gemini 이미지 모델을 사용합니다.
        if (model == null || model.isBlank()) {
            return "gemini-3-pro-image-preview";
        }
        return model;
    }

    private String applySizeHint(String prompt, String size) {
        // Gemini가 해상도 파라미터를 받지 않으므로 프롬프트에 힌트만 추가합니다.
        String normalized = normalizeSize(size);
        if (normalized == null) {
            return prompt;
        }
        return prompt + "\n\nResolution: " + normalized + ".";
    }

    private String normalizeSize(String size) {
        // 1k/2k/4k 또는 1024x1024 같은 입력을 표준 해상도로 정규화합니다.
        if (size == null || size.isBlank()) {
            return null;
        }
        String trimmed = size.trim().toLowerCase();
        if ("1k".equals(trimmed)) return "1024x1024";
        if ("2k".equals(trimmed)) return "2048x2048";
        if ("4k".equals(trimmed)) return "4096x4096";
        if (trimmed.matches("\\d{3,4}x\\d{3,4}")) {
            return trimmed;
        }
        return null;
    }
}
