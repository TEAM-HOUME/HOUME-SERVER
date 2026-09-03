package or.sopt.houme.priceCompare.domain.port.out;

import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;

/**
 * 외부 상품 페이지 스크래핑 아웃바운드 포트.
 * 애플리케이션은 이 인터페이스만 알고, HTTP 호출·HTML 파싱은 houme-external 어댑터가 제공한다.
 */
public interface ProductPageScrapePort {

    /**
     * 상품 페이지를 내려받아 메타데이터를 추출한다.
     * 일부 필드만 확보되는 부분 성공이 정상 결과이며, 완성도는 {@link ScrapedProduct#quality()} 로 판단한다.
     */
    ScrapedProduct scrape(SourceUrl sourceUrl);
}
