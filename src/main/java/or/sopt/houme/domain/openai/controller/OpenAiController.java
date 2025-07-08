package or.sopt.houme.domain.openai.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.openai.facade.OpenAiFacadeImpl;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class OpenAiController {

    private final OpenAiFacadeImpl openAiFacade;

    @Operation(summary = "chatgpt-image-1 을 활용한 이미지 저장 API",
        description = "실제 성능 테스트를 위한 메서드입니다. **호출 시, 서버 전재연에게 반드시 문의해주세요**")
    @PostMapping(value = "/generate")
    public ResponseEntity<ApiResponse<String>> generate() {

        ImageUploadResponseDTO responseDTO = openAiFacade.makeImage(new PromptRequestDTO());

        return ResponseEntity.ok().body(ApiResponse.ok(responseDTO.getImageLink()));
    }

}
