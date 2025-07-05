package or.sopt.houme.domain.house.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;

    // 집구조 제공 API
    @Operation(summary = "집구조 제공 API",
            description = "집 구조 관련 리스트를 제공해줍니다. (주거형태, 공간구조, 평형) 옵션들을 제공합니다.")
    @GetMapping("/housing-options")
    public ResponseEntity<ApiResponse<HouseOptionsResponse>> housingOptions() {
        return ResponseEntity.ok(ApiResponse.ok(houseService.getHouseOptionsResponse()));
    }
}
