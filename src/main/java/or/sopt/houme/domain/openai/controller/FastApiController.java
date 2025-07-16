package or.sopt.houme.domain.openai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.openai.service.FastApiService;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.constant.S3Constant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v2/image")
@Tag(name = "LLM 호출로직 테스트 API")
public class FastApiController {

    private final FastApiService fastApiService;

    @Operation(summary = "LangChain 을 활용한 이미지 저장 API",
            description = "실제 성능 테스트를 위한 메서드입니다. **호출 시, 서버 전재연에게 반드시 문의해주세요**")
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<String>> generate(@RequestBody PromptRequestDTO promptRequestDTO) {

        ImageUploadResponseDTO responseDTO = fastApiService.getImageByFastApi(promptRequestDTO);

        if (responseDTO.getImageLink().equals(S3Constant.FALL_BACK_IMAGE)){
            return ResponseEntity.badRequest().body(ApiResponse.fail(500,responseDTO.getImageLink(),"이미지 생성 중 예외가 발생하였습니다"));
        }

        return ResponseEntity.ok().body(ApiResponse.ok(responseDTO.getImageLink()));
    }
}
