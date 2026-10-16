package or.sopt.houme.priceCompare;

import or.sopt.houme.coupang.domain.CoupangProduct;
import or.sopt.houme.coupang.domain.port.out.CoupangProductPort;
import or.sopt.houme.priceCompare.application.SourceProductProvider;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.CoupangShortLinkResolvePort;
import or.sopt.houme.priceCompare.domain.port.out.ProductPageScrapePort;
import or.sopt.houme.priceCompare.domain.port.out.ScrapedProductCachePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("상품 URL 메타데이터 조회 — 쿠팡 상품 페이지 요청 최소화")
class SourceProductProviderTest {

    private static final String COUPANG_URL =
            "https://www.coupang.com/vp/products/7335597976?itemId=18855165010&vendorItemId=86960985090";
    private static final SourceUrl COUPANG_CANONICAL = SourceUrl.normalize(COUPANG_URL);
    private static final SourceUrl SHORT_LINK = SourceUrl.normalize("https://link.coupang.com/a/bQ7xyz");

    private final ProductPageScrapePort scrapePort = mock(ProductPageScrapePort.class);
    private final CoupangProductPort coupangProductPort = mock(CoupangProductPort.class);
    private final CoupangShortLinkResolvePort shortLinkResolvePort = mock(CoupangShortLinkResolvePort.class);
    private final ScrapedProductCachePort cachePort = mock(ScrapedProductCachePort.class);
    private final SourceProductProvider provider =
            new SourceProductProvider(scrapePort, coupangProductPort, shortLinkResolvePort, cachePort);

    @Test
    @DisplayName("카탈로그에 있는 쿠팡 상품은 페이지를 긁지 않고 카탈로그 값으로 응답한다")
    void 카탈로그에_있으면_스크래핑하지_않는다() {
        when(coupangProductPort.findByCoupangProductId("7335597976")).thenReturn(Optional.of(catalogProduct()));

        ScrapedProduct product = provider.provide(COUPANG_CANONICAL);

        verify(scrapePort, never()).scrape(any());
        assertThat(product.sourceUrl()).isEqualTo("https://www.coupang.com/vp/products/7335597976");
        assertThat(product.title()).isEqualTo("힘내바 초코 스니커즈, 480g, 1개");
        assertThat(product.price()).isEqualTo(14770L);
        assertThat(product.currency()).isEqualTo("KRW");
        assertThat(product.hasEssentials()).isTrue();
    }

    @Test
    @DisplayName("카탈로그에 없어도 캐시에 있으면 페이지를 긁지 않는다")
    void 캐시에_있으면_스크래핑하지_않는다() {
        ScrapedProduct cached = scraped(COUPANG_CANONICAL);
        when(cachePort.find(COUPANG_CANONICAL)).thenReturn(Optional.of(cached));

        assertThat(provider.provide(COUPANG_CANONICAL)).isEqualTo(cached);
        verify(scrapePort, never()).scrape(any());
    }

    @Test
    @DisplayName("카탈로그·캐시에 모두 없으면 정규화된 URL 로 긁고, 필수값을 다 건지면 캐시에 남긴다")
    void 스크래핑_성공_결과는_캐시에_남긴다() {
        ScrapedProduct scraped = scraped(COUPANG_CANONICAL);
        when(scrapePort.scrape(COUPANG_CANONICAL)).thenReturn(scraped);

        assertThat(provider.provide(COUPANG_CANONICAL)).isEqualTo(scraped);
        verify(cachePort).save(scraped);
    }

    @Test
    @DisplayName("필수값이 빠진 스크래핑 결과는 캐시에 남기지 않는다")
    void 불완전한_스크래핑_결과는_캐시하지_않는다() {
        ScrapedProduct noPrice = new ScrapedProduct(COUPANG_CANONICAL.value(), "소파", "https://cdn/a.jpg",
                null, null, null, List.of(), null);
        when(scrapePort.scrape(COUPANG_CANONICAL)).thenReturn(noPrice);

        provider.provide(COUPANG_CANONICAL);

        verify(cachePort, never()).save(any());
    }

    @Test
    @DisplayName("카탈로그 상품에 이미지가 없으면 필수값을 채우려고 페이지를 긁는다")
    void 카탈로그_값이_불완전하면_스크래핑한다() {
        ScrapedProduct scraped = scraped(COUPANG_CANONICAL);
        when(coupangProductPort.findByCoupangProductId("7335597976")).thenReturn(Optional.of(new CoupangProduct(
                1L, "소파", null, "https://link.coupang.com/re/x", 100000L, 100000L, 0)));
        when(scrapePort.scrape(COUPANG_CANONICAL)).thenReturn(scraped);

        assertThat(provider.provide(COUPANG_CANONICAL)).isEqualTo(scraped);
    }

    @Test
    @DisplayName("단축 링크는 리다이렉트로 푼 상품 URL 로 카탈로그를 조회한다")
    void 단축_링크를_풀어_카탈로그를_조회한다() {
        when(shortLinkResolvePort.resolve(SHORT_LINK)).thenReturn(Optional.of(COUPANG_CANONICAL));
        when(coupangProductPort.findByCoupangProductId("7335597976")).thenReturn(Optional.of(catalogProduct()));

        ScrapedProduct product = provider.provide(SHORT_LINK);

        verify(scrapePort, never()).scrape(any());
        assertThat(product.sourceUrl()).isEqualTo(COUPANG_CANONICAL.value());
    }

    @Test
    @DisplayName("단축 링크를 풀지 못하면 원래 링크 그대로 긁는다")
    void 단축_링크를_못_풀면_원래_링크를_긁는다() {
        ScrapedProduct scraped = scraped(SHORT_LINK);
        when(shortLinkResolvePort.resolve(SHORT_LINK)).thenReturn(Optional.empty());
        when(scrapePort.scrape(SHORT_LINK)).thenReturn(scraped);

        assertThat(provider.provide(SHORT_LINK)).isEqualTo(scraped);
        verify(coupangProductPort, never()).findByCoupangProductId(anyString());
    }

    @Test
    @DisplayName("쿠팡이 아닌 몰은 카탈로그·캐시를 거치지 않고 바로 긁는다")
    void 쿠팡이_아니면_바로_스크래핑한다() {
        SourceUrl ohou = SourceUrl.normalize("https://ohou.se/productions/123");
        when(scrapePort.scrape(ohou)).thenReturn(scraped(ohou));

        provider.provide(ohou);

        verify(shortLinkResolvePort, never()).resolve(any());
        verify(coupangProductPort, never()).findByCoupangProductId(anyString());
        verify(cachePort, never()).find(any());
        verify(cachePort, never()).save(any());
    }

    private static CoupangProduct catalogProduct() {
        return new CoupangProduct(1L, "힘내바 초코 스니커즈, 480g, 1개", "https://thumbnail.coupangcdn.com/a.jpg",
                "https://link.coupang.com/re/AFFSDP?pageKey=7335597976", 14770L, 16400L, 10);
    }

    private static ScrapedProduct scraped(SourceUrl sourceUrl) {
        return new ScrapedProduct(sourceUrl.value(), "소파", "https://cdn/a.jpg",
                null, 100000L, "KRW", List.of(), null);
    }
}
