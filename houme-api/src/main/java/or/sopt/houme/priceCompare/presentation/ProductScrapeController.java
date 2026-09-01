package or.sopt.houme.priceCompare.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.priceCompare.application.ProductScrapeUseCase;
import or.sopt.houme.priceCompare.application.dto.ProductScrapeRequest;
import or.sopt.houme.priceCompare.application.dto.ScrapedProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "가격 비교 관련 API")
public class ProductScrapeController {

    private final ProductScrapeUseCase productScrapeUseCase;

    @PostMapping("/price-compare/scrape")
    @Operation(summary = "외부 상품 URL 메타데이터 추출 API",
            description = "외부 쇼핑몰 상품 URL에서 상품명·이미지·브랜드·가격을 추출합니다. "
                    + "quality 필드로 추출 완성도(FULL/PARTIAL/MINIMAL)를 함께 반환합니다.")
    public ResponseEntity<ApiResponse<ScrapedProductResponse>> scrape(
            @Valid @RequestBody ProductScrapeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productScrapeUseCase.scrape(request.url())));
    }
}
