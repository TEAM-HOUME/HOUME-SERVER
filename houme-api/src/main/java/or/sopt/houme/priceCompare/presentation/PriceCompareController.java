package or.sopt.houme.priceCompare.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.priceCompare.application.PriceCompareUseCase;
import or.sopt.houme.priceCompare.application.dto.PriceCompareStartResponse;
import or.sopt.houme.priceCompare.application.dto.ProductScrapeRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/price-compare")
@RequiredArgsConstructor
@Tag(name = "가격 비교 관련 API")
public class PriceCompareController {

    private final PriceCompareUseCase priceCompareUseCase;

    @PostMapping
    @Operation(
            summary = "가격 비교 파이프라인 시작 API",
            description = "외부 쇼핑몰 상품 URL을 입력하면 상품 정보를 즉시 추출하고 가격 비교 파이프라인을 시작합니다. "
                    + "jobId로 이후 결과를 조회할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<PriceCompareStartResponse>> start(
            @Valid @RequestBody ProductScrapeRequest request) {
        PriceCompareStartResponse response = priceCompareUseCase.start(request.url());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ApiResponse<>(202, "응답 성공", response));
    }
}
