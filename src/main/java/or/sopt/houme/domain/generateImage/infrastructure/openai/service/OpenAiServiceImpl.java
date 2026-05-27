package or.sopt.houme.domain.generateImage.infrastructure.openai.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.infrastructure.openai.client.OpenAIImageClient;
import or.sopt.houme.domain.generateImage.presentation.openai.controller.dto.OpenAiRequest;
import or.sopt.houme.domain.generateImage.presentation.openai.controller.dto.OpenAiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.ChatGptException;
import or.sopt.houme.global.api.handler.S3Exception;
import or.sopt.houme.global.config.OpenAiImageConfig;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.S3Util;
import or.sopt.houme.global.util.constant.S3Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static or.sopt.houme.global.logging.LogMarkers.fields;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiServiceImpl implements OpenAiService {

    private final OpenAIImageClient openAIImageClient;
    private final S3Util s3Util;
    private final OpenAiImageConfig openAiImageConfig;

    @Value("${openai.api-key}")
    private String apiKey;


    /**
     * 프롬프트를 받으면
     * 해당 프롬프트를 기반으로 이미지를 생성합니다
     *
     * 해당 이미지를 S3에 저장합니다
     * */
    @Override
    public ImageUploadResponseDTO createImage(String prompt) {
        long startTime = System.nanoTime();

        // 요청을 위한 객체를 생성
        OpenAiRequest request = OpenAiRequest.of(
                openAiImageConfig.getModel(),
                prompt,
                openAiImageConfig.getN(),
                openAiImageConfig.getSize(),
                openAiImageConfig.getQuality(),
                openAiImageConfig.getBackground(),
                openAiImageConfig.getOutputFormat()
        );

        try {
            log.info(
                    fields(
                            "event", "image.ai.request.started",
                            "provider", "openai",
                            "model", openAiImageConfig.getModel(),
                            "promptLength", prompt.length(),
                            "size", openAiImageConfig.getSize(),
                            "quality", openAiImageConfig.getQuality()
                    ),
                    "image ai request started"
            );
            byte[] image = getGptImage(request);

            // S3에 이미지 저장하고 메타데이터를 반환
            ImageUploadResponseDTO responseDTO = s3Util.uploadByByte(S3Constant.CHAT_GPT_DIRNAME, image);
            responseDTO.setPullPrompt(prompt);

            log.info(
                    fields(
                            "event", "image.ai.request.succeeded",
                            "provider", "openai",
                            "model", openAiImageConfig.getModel(),
                            "durationMs", elapsedMillis(startTime),
                            "imageBytes", image.length
                    ),
                    "image ai request succeeded"
            );
            return responseDTO;

        } catch (FeignException e) {
            log.error(
                    fields(
                            "event", "image.ai.request.failed",
                            "provider", "openai",
                            "model", openAiImageConfig.getModel(),
                            "durationMs", elapsedMillis(startTime),
                            "status", e.status(),
                            "exceptionType", e.getClass().getSimpleName(),
                            "errorMessage", e.getMessage()
                    ),
                    "image ai request failed",
                    e
            );
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        }
    }




    @Override
    public byte[] getGptImage(OpenAiRequest request) {

        OpenAiResponse response = openAIImageClient.generateImage("Bearer " + apiKey, request);

        if (response.getData() == null || response.getData().isEmpty()) {
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        }

        try {
            // 응답 타입인 base64로 인코딩된 string 을 이미지로 변환
            String b64 = response.getData().get(0).getB64_json();
            return Base64.getDecoder().decode(b64);
        }catch (IllegalArgumentException e){
            log.error(
                    fields(
                            "event", "image.ai.response.decode_failed",
                            "provider", "openai",
                            "exceptionType", e.getClass().getSimpleName(),
                            "errorMessage", e.getMessage()
                    ),
                    "image ai response decode failed",
                    e
            );
            throw new S3Exception(ErrorCode.INCODING_EXCEPTION);
        }
    }

    private long elapsedMillis(long startTime) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

}
