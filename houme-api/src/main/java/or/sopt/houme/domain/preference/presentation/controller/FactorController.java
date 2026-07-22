package or.sopt.houme.domain.preference.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.preference.presentation.dto.response.FactorsResponse;
import or.sopt.houme.domain.preference.service.FactorService;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "요인 API")
public class FactorController {

    private final FactorService factorService;

    @Operation(summary = "요인 문구 제공 API",
            description = "좋아요 여부에 따른 요인 문구를 제공합니다.")
    @GetMapping("/factors")
    public ResponseEntity<ApiResponse<FactorsResponse>> getFactors(@RequestParam boolean isLike) {
        FactorsResponse response = factorService.getFactors(isLike);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "생성된 이미지 선호 여부에 따른 요인 선택 로그 토글 API",
            description = "생성된 이미지의 선호 여부에 따른 요인을 선택한 로그를 토글링합니다.")
    @PostMapping("/generated-images/{imageId}/preference/factors/{factorId}")
    public ResponseEntity<ApiResponse<Void>> toggleFactorLog(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long imageId,
            @PathVariable Long factorId
    ) {
        // 유저와 이미지 사이 preference객체는 하나이지만, factor는 좋아요 객체에 매핑된 여러개의 객체가 존재
        // 유저와 이미지에 따른 요인 객체를 찾아 토글링합니다.
        factorService.toggleFactorLog(user.getUser(), imageId, factorId);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
