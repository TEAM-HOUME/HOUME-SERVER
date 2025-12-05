package or.sopt.houme.ai.image;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import or.sopt.houme.domain.openai.client.FastApiImageClient;
import or.sopt.houme.domain.openai.service.OpenAiService;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.prompt.service.PromptService;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;

/**
 * FastAPI 의존 제거를 위한 로컬 어댑터 구현체입니다.
 *
 * 기존 FastAPI 연동 인터페이스(FastApiImageClient)를 구현하여
 * 외부 HTTP 호출 없이 Spring 내부 로직으로 이미지 생성을 수행합니다.
 *
 * 동작 흐름
 * 1) PromptService.makePrompt(...)로 최종 프롬프트 생성
 * 2) OpenAiService.createImage(prompt) 호출로 OpenAI 이미지 생성 및 S3 업로드
 * 3) ImageUploadResponseDTO 반환 (clipScore는 제거)
 */
@Component
@Primary
@RequiredArgsConstructor
public class LocalFastApiImageClient implements FastApiImageClient {

    private final PromptService promptService;
    private final OpenAiService openAiService;

    /**
     * FastAPI의 /images 엔드포인트 대체 구현.
     *
     * @param request 프롬프트 구성에 필요한 식별자/옵션 DTO
     * @return S3 업로드 메타데이터가 포함된 응답 DTO
     */
    @Override
    public ImageUploadResponseDTO generateImage(PromptRequestDTO request) {
        // 1) 프롬프트 합성
        String prompt = promptService.makePrompt(request);

        // 2) 이미지 생성 + S3 업로드 (내부 서비스 활용)
        ImageUploadResponseDTO response = openAiService.createImage(prompt);

        // pullPrompt 정보 보강 (기존 FastAPI 응답 정합성 유지)
        response.setPullPrompt(prompt);
        return response;
    }
}

