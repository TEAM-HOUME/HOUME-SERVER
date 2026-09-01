package or.sopt.houme.compare.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import or.sopt.houme.compare.presentation.dto.response.PresetDetailResponse;
import or.sopt.houme.compare.presentation.dto.response.PresetListResponse;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/price-compare")
@Tag(name = "가격비교 API")
public class PresetController {

    @Operation(summary = "프리셋 목록 조회")
    @GetMapping("/presets")
    public ResponseEntity<ApiResponse<PresetListResponse>> getPresets() {
        // TODO: implement
        return ResponseEntity.ok(ApiResponse.ok(new PresetListResponse(List.of())));
    }

    @Operation(summary = "프리셋 가격비교 결과 조회")
    @GetMapping("/presets/{presetId}")
    public ResponseEntity<ApiResponse<PresetDetailResponse>> getPresetDetail(
            @PathVariable Long presetId
    ) {
        // TODO: implement
        return ResponseEntity.ok(ApiResponse.ok(new PresetDetailResponse(null, List.of(), 0)));
    }
}
