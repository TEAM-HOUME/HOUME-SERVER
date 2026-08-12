package or.sopt.houme.domain.house.presentation.taste.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.presentation.taste.dto.response.MoodBoardListResponse;
import or.sopt.houme.domain.house.service.taste.TasteService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "무드보드 API")
@RequiredArgsConstructor
public class TasteController {

    private final TasteService tasteService;

    @Operation(summary = "무드보드 제공 API",
            description = "무드보드 전체를 조회합니다. (커서 기반 페이지네이션)")
    @GetMapping("/moodboard-images")
    public ResponseEntity<ApiResponse<MoodBoardListResponse>> moodboardImages(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") @Valid @Min(1) int limit
    ) {

        MoodBoardListResponse moodboard = tasteService.getMoodboard(cursor, limit);

        return ResponseEntity.ok(ApiResponse.ok(moodboard));
    }
}
