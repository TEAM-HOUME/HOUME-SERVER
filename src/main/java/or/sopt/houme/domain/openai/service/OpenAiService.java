package or.sopt.houme.domain.openai.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.openai.client.OpenAIImageClient;
import or.sopt.houme.domain.openai.controller.dto.OpenAiRequest;
import or.sopt.houme.domain.openai.controller.dto.OpenAiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.ChatGptException;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.S3Util;
import or.sopt.houme.global.util.constant.S3DirNameConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final OpenAIImageClient openAIImageClient;
    private final S3Util s3Util;

    @Value("${openai.api-key}")
    private String apiKey;


    /**
     * 프롬프트를 받으면
     * 해당 프롬프트를 기반으로 이미지를 생성합니다
     *
     * 해당 이미지를 S3에 저장합니다
     * */
    public ImageUploadResponseDTO createImage(String prompt) {

        // 요청을 위한 객체를 생성
        OpenAiRequest request = OpenAiRequest.of(prompt);

        try {
            byte[] image = getGptImage(request);

            // S3에 이미지 저장하고 메타데이터를 반환
            return s3Util.uploadByByte(S3DirNameConstant.CHAT_GPT_DIRNAME, image);

        } catch (FeignException e) {
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        }
    }



    private byte[] getGptImage(OpenAiRequest request) {
        OpenAiResponse response = openAIImageClient.generateImage("Bearer " + apiKey, request);

        if (response.getData() == null || response.getData().isEmpty()) {
            return null;
        }

        // 응답 타입인 base64로 인코딩된 string 을 이미지로 변환
        String b64 = response.getData().get(0).getB64_json();
        return Base64.getDecoder().decode(b64);
    }

}
