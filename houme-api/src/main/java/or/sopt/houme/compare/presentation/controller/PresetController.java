package or.sopt.houme.compare.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.dto.PresetDetailResponse;
import or.sopt.houme.compare.application.dto.PresetListResponse;
import or.sopt.houme.compare.domain.port.in.PresetUseCase;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/price-compare")
@RequiredArgsConstructor
@Tag(name = "가격비교 API")
public class PresetController {

    private final PresetUseCase presetUseCase;

    @Operation(summary = "프리셋 목록 조회")
    @GetMapping("/presets")
    public ResponseEntity<ApiResponse<PresetListResponse>> getPresets() {
        return ResponseEntity.ok(ApiResponse.ok(PresetListResponse.from(presetUseCase.getPresets())));
    }

    @Operation(summary = "프리셋 가격비교 결과 조회")
    @GetMapping("/presets/{presetId}")
    public ResponseEntity<ApiResponse<PresetDetailResponse>> getPresetDetail(
            @PathVariable Long presetId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(PresetDetailResponse.from(presetUseCase.getPresetDetail(presetId))));
    }
}
