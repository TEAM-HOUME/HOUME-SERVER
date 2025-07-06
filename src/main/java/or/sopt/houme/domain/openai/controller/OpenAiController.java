package or.sopt.houme.domain.openai.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.openai.service.OpenAiService;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class OpenAiController {

    private final OpenAiService imageService;

    @Operation(summary = "chatgpt-image-1 을 활용한 이미지 저장 API",
        description = "실제 성능 테스트를 위한 메서드입니다. **호출 시, 서버 전재연에게 반드시 문의해주세요**")
    @GetMapping(value = "/generate")
    public ResponseEntity<String> generate(@RequestParam String prompt) {

        ImageUploadResponseDTO response = imageService.createImage(prompt);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(response.getImageLink());
    }

}
