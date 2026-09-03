package or.sopt.houme.compare.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.dto.PresetListResponse;
import or.sopt.houme.compare.application.dto.SavePresetRequest;
import or.sopt.houme.compare.domain.port.in.PresetUseCase;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/compare/presets")
@RequiredArgsConstructor
@Tag(name = "어드민 가격비교 프리셋 API")
public class AdminPresetController {

    private final PresetUseCase presetUseCase;

    @Operation(summary = "프리셋 목록 조회 (어드민)")
    @GetMapping
    public ResponseEntity<ApiResponse<PresetListResponse>> getPresets() {
        return ResponseEntity.ok(ApiResponse.ok(PresetListResponse.from(presetUseCase.getPresets())));
    }

    @Operation(summary = "프리셋 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPreset(@RequestBody @Valid SavePresetRequest request) {
        Long presetId = presetUseCase.createPreset(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "응답 성공", presetId));
    }

    @Operation(summary = "프리셋 수정")
    @PutMapping("/{presetId}")
    public ResponseEntity<ApiResponse<Void>> updatePreset(
            @PathVariable Long presetId,
            @RequestBody @Valid SavePresetRequest request
    ) {
        presetUseCase.updatePreset(presetId, request.toDomain());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "프리셋 삭제")
    @DeleteMapping("/{presetId}")
    public ResponseEntity<Void> deletePreset(@PathVariable Long presetId) {
        presetUseCase.deletePreset(presetId);
        return ResponseEntity.noContent().build();
    }
}
