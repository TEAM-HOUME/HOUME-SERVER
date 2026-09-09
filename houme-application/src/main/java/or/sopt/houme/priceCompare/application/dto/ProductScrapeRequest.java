package or.sopt.houme.priceCompare.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 비교하려는 외부 상품 URL 입력.
 * 형식 검증은 여기서 존재 여부만 보고, 실제 정규화/차단 판단은 도메인이 담당한다.
 */
public record ProductScrapeRequest(
        @NotBlank(message = "상품 URL은 비어 있을 수 없습니다.")
        String url
) {
}
