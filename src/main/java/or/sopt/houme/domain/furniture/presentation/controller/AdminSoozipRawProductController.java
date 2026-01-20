package or.sopt.houme.domain.furniture.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.presentation.dto.response.SoozipRawProductSaveResponse;
import or.sopt.houme.domain.furniture.service.CurationRawProductService;
import or.sopt.houme.domain.furniture.service.SoozipCrawlingService;
import or.sopt.houme.domain.furniture.service.dto.CurationRawProductSaveResult;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/curations/soozip")
@Tag(name = "어드민 큐레이션 원본 API")
public class AdminSoozipRawProductController {

    private static final String SOURCE_SOOZIP = "soozip";

    private final SoozipCrawlingService soozipCrawlingService;
    private final CurationRawProductService curationRawProductService;

    @PostMapping("/raw")
    @Operation(summary = "Soozip 원본 수집 저장 API",
            description = "Soozip 리스트를 크롤링하여 curation_raw_products에 저장합니다.\n\n" +
                    "카테고리 번호:\n" +
                    "- 73: 미니가전\n" +
                    "- 75: 가구\n" +
                    "- 76: 조명\n" +
                    "- 86: 생활용품\n" +
                    "- 47: 홈패브릭\n" +
                    "- 52: 소품")
    public ResponseEntity<ApiResponse<SoozipRawProductSaveResponse>> saveSoozipRawProducts(
            @Parameter(
                    description = "Soozip 카테고리 번호 (기본값=75).",
                    example = "75"
            )
            @RequestParam(required = false, defaultValue = "75") Integer cateNo,
            @Parameter(
                    description = "Soozip 목록에서 크롤링할 최대 페이지 수. 미지정 시 마지막 페이지까지 순회합니다.",
                    example = "1"
            )
            @RequestParam(required = false) Integer maxPages
    ) {
        SoozipCategory category = SoozipCategory.fromCateNo(cateNo)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_VALID_EXCEPTION));
        List<NaverFurnitureProductDto> products = soozipCrawlingService.fetchCategoryProducts(cateNo, maxPages);
        CurationRawProductSaveResult result = curationRawProductService.saveAll(SOURCE_SOOZIP, category, products);
        SoozipRawProductSaveResponse response = SoozipRawProductSaveResponse.of(
                SOURCE_SOOZIP,
                category,
                products.size(),
                result
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
