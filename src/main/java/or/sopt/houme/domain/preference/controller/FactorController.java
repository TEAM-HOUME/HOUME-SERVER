package or.sopt.houme.domain.preference.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.preference.dto.response.FactorsResponse;
import or.sopt.houme.domain.preference.service.FactorService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
