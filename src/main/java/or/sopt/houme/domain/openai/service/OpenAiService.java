package or.sopt.houme.domain.openai.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.openai.client.OpenAIImageClient;
import or.sopt.houme.domain.openai.controller.dto.OpenAiRequest;
import or.sopt.houme.domain.openai.controller.dto.OpenAiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.ChatGptException;
import or.sopt.houme.global.util.S3Util;
import or.sopt.houme.global.util.constant.S3DirNameConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private final OpenAIImageClient openAIImageClient;
    private final S3Util s3Util;

    @Value("${openai.api-key}")
    private String apiKey;


    /**
     * 사용자의 정보를 모두 받고
     * 그 정보를 기반으로 프롬프트를 작성하고
     * 그 프롬프트를 해당 메서드에 넣으면 이미지가 생성된다
     * */
    public byte[] createImage(String prompt) {

        // 요청을 위한 객체를 생성
        OpenAiRequest request = OpenAiRequest.of(prompt);

        try {
            byte[] image = getGptImage(request);

            // 비동기로 S3에 이미지 저장
            s3Util.upload(image, S3DirNameConstant.CHAT_GPT_DIRNAME);

            return image;

        } catch (FeignException e) {
            log.error("OpenAI 호출 실패: status={}, content={}", e.status(), e.contentUTF8());
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        }
    }



    private byte[] getGptImage(OpenAiRequest request) {
        OpenAiResponse response = openAIImageClient.generateImage("Bearer " + apiKey, request);

        if (response.getData() == null || response.getData().isEmpty()) {
            log.error("OpenAI 응답 데이터가 비어 있음");
            return null;
        }

        // 응답 타입인 base64로 인코딩된 string 을 이미지로 변환
        String b64 = response.getData().get(0).getB64_json();
        return Base64.getDecoder().decode(b64);
    }

}
