package or.sopt.houme.priceCompare.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.coupang.domain.CoupangProduct;
import or.sopt.houme.coupang.domain.port.out.CoupangProductPort;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.ProductPageScrapePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 유저가 넣은 상품 URL 의 메타데이터를 가져온다.
 *
 * <p>쿠팡은 서버 IP 의 연속 요청을 403 으로 막고, 한 번 막히면 수십 분간 풀리지 않는다.
 * 그래서 쿠팡 URL 은 페이지를 긁기 전에 파트너스 API 로 수집해 둔 자체 카탈로그를 먼저 본다.
 * 카탈로그에 없거나 필수값이 비어 있을 때만 페이지 스크래핑으로 넘어간다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SourceProductProvider {

    private static final String COUPANG_CURRENCY = "KRW";

    private final ProductPageScrapePort productPageScrapePort;
    private final CoupangProductPort coupangProductPort;

    public ScrapedProduct provide(SourceUrl sourceUrl) {
        return fromCoupangCatalog(sourceUrl)
                .orElseGet(() -> productPageScrapePort.scrape(sourceUrl));
    }

    private Optional<ScrapedProduct> fromCoupangCatalog(SourceUrl sourceUrl) {
        return sourceUrl.coupangProductId()
                .flatMap(coupangProductPort::findByCoupangProductId)
                .map(product -> toScrapedProduct(sourceUrl, product))
                .filter(ScrapedProduct::hasEssentials)
                .map(product -> {
                    log.info("쿠팡 카탈로그에서 상품 조회: url={}", sourceUrl.value());
                    return product;
                });
    }

    private ScrapedProduct toScrapedProduct(SourceUrl sourceUrl, CoupangProduct product) {
        return new ScrapedProduct(
                sourceUrl.value(),
                product.name(),
                product.imageUrl(),
                null,
                product.currentPrice(),
                COUPANG_CURRENCY,
                List.of(),
                null
        );
    }
}
