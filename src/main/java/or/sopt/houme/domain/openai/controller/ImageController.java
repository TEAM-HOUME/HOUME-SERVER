package or.sopt.houme.domain.openai.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.openai.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "chatgpt-image-1 을 활용한 이미지 저장 API",
        description = "실제 성능 테스트를 위한 메서드입니다. **호출 시, 서버 전재연에게 반드시 문의해주세요**")
    @GetMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generate(@RequestParam String prompt) {

        byte[] image = imageService.createImage(prompt);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(image);
    }

}
