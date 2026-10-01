package or.sopt.houme.priceCompare.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.coupang.domain.CoupangProduct;
import or.sopt.houme.coupang.domain.port.out.CoupangProductPort;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.CoupangShortLinkResolvePort;
import or.sopt.houme.priceCompare.domain.port.out.ProductPageScrapePort;
import or.sopt.houme.priceCompare.domain.port.out.ScrapedProductCachePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 유저가 넣은 상품 URL 의 메타데이터를 가져온다.
 *
 * <p>쿠팡은 서버 IP 의 연속 요청을 403 으로 막고, 한 번 막히면 수십 분간 풀리지 않는다.
 * 그래서 쿠팡 상품 페이지 요청을 최대한 피하는 순서로 찾는다.
 * <ol>
 *     <li>단축 링크면 리다이렉트만 풀어 상품 URL 을 얻는다 (상품 페이지는 요청하지 않음)</li>
 *     <li>파트너스 API 로 수집해 둔 자체 카탈로그</li>
 *     <li>앞서 스크래핑해 둔 결과 캐시</li>
 *     <li>그래도 없으면 페이지 스크래핑 — 필수값을 다 건졌을 때만 캐시에 남긴다</li>
 * </ol>
 * 쿠팡이 아닌 몰은 기존대로 바로 스크래핑한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SourceProductProvider {

    private static final String COUPANG_CURRENCY = "KRW";

    private final ProductPageScrapePort productPageScrapePort;
    private final CoupangProductPort coupangProductPort;
    private final CoupangShortLinkResolvePort coupangShortLinkResolvePort;
    private final ScrapedProductCachePort scrapedProductCachePort;

    public ScrapedProduct provide(SourceUrl sourceUrl) {
        SourceUrl target = sourceUrl.isCoupangShortLink()
                ? coupangShortLinkResolvePort.resolve(sourceUrl).orElse(sourceUrl)
                : sourceUrl;

        if (target.coupangProductId().isEmpty()) {
            return productPageScrapePort.scrape(target);
        }
        return fromCoupangCatalog(target)
                .or(() -> fromCache(target))
                .orElseGet(() -> scrapeAndCache(target));
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

    private Optional<ScrapedProduct> fromCache(SourceUrl sourceUrl) {
        return scrapedProductCachePort.find(sourceUrl)
                .map(product -> {
                    log.info("스크래핑 캐시에서 상품 조회: url={}", sourceUrl.value());
                    return product;
                });
    }

    private ScrapedProduct scrapeAndCache(SourceUrl sourceUrl) {
        ScrapedProduct scraped = productPageScrapePort.scrape(sourceUrl);
        if (scraped.hasEssentials()) {
            scrapedProductCachePort.save(scraped);
        }
        return scraped;
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
