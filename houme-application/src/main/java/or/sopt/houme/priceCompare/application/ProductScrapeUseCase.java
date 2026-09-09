package or.sopt.houme.priceCompare.application;

import or.sopt.houme.priceCompare.application.dto.ScrapedProductResponse;

/**
 * 외부 상품 URL 메타데이터 추출 인바운드 포트.
 * 가격 비교 파이프라인의 첫 단계이며, 이후 eBay·쿠팡·자체 카탈로그 검색의 입력을 만든다.
 */
public interface ProductScrapeUseCase {

    /** 유저가 입력한 원시 URL을 정규화·검증한 뒤 상품 메타데이터를 추출한다. */
    ScrapedProductResponse scrape(String rawUrl);
}
