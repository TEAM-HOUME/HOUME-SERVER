package or.sopt.houme.priceCompare.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;
import or.sopt.houme.priceCompare.application.dto.ScrapedProductResponse;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.ProductPageScrapePort;
import org.springframework.stereotype.Service;

/**
 * 상품 URL 메타데이터 추출 유스케이스.
 *
 * <p>DB를 건드리지 않고 외부 HTTP 호출만 하므로 트랜잭션 경계를 두지 않는다
 * (외부 호출을 트랜잭션 안에 넣지 않는다는 규칙과도 맞는다).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductScrapeService implements ProductScrapeUseCase {

    private final ProductPageScrapePort productPageScrapePort;

    @Override
    public ScrapedProductResponse scrape(String rawUrl) {
        SourceUrl sourceUrl = SourceUrl.normalize(rawUrl);
        ScrapedProduct product = productPageScrapePort.scrape(sourceUrl);

        // 상품명·이미지를 하나도 못 건지거나 가격이 없으면 이후 비교를 태울 입력이 안 되므로 여기서 끊는다.
        if (!product.hasEssentials()) {
            log.warn("상품 메타데이터 추출 실패 - 필수 정보 없음: url={}", sourceUrl.value());
            throw new PriceCompareException(ErrorCode.PRODUCT_METADATA_PARSE_FAILED);
        }
        return ScrapedProductResponse.from(product);
    }
}
