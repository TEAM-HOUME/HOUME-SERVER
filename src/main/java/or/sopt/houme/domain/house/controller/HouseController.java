package or.sopt.houme.domain.house.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.dto.request.HouseSelectRequest;
import or.sopt.houme.domain.house.dto.response.HouseIdResponse;
import or.sopt.houme.domain.house.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "집 구조 API")
public class HouseController {

    private final HouseService houseService;

    // 집구조 제공 API
    @Operation(summary = "집구조 제공 API",
            description = "집 구조 관련 리스트를 제공해줍니다. (주거형태, 공간구조, 평형) 옵션들을 제공합니다.")
    @GetMapping("/housing-options")
    public ResponseEntity<ApiResponse<HouseOptionsResponse>> housingOptions() {
        return ResponseEntity.ok(ApiResponse.ok(houseService.getHouseOptionsResponse()));
    }

    // 집 구조 선택 API
    @Operation(summary = "집 구조 선택 API",
            description = "집 구조를 선택받고 저장합니다. (주거형태, 공간구조, 평형) 옵션들을 저장합니다.")
    @PostMapping("/housing-selections")
    public ResponseEntity<ApiResponse<HouseIdResponse>>  housingSelections(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                           @Valid @RequestBody HouseSelectRequest houseSelectRequest) {

        HouseIdResponse houseId = houseService.selectHouseOptions(userDetails.getUser(), houseSelectRequest);
        return ResponseEntity.ok(ApiResponse.ok(houseId));
    }
}
