package or.sopt.houme.priceCompare;

import or.sopt.houme.coupang.domain.CoupangProduct;
import or.sopt.houme.coupang.domain.port.out.CoupangProductPort;
import or.sopt.houme.priceCompare.application.SourceProductProvider;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.ProductPageScrapePort;
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

@DisplayName("상품 URL 메타데이터 조회 — 쿠팡 카탈로그 우선")
class SourceProductProviderTest {

    private static final String COUPANG_URL =
            "https://www.coupang.com/vp/products/7335597976?itemId=18855165010&vendorItemId=86960985090";
    private static final String COUPANG_CANONICAL_URL = "https://www.coupang.com/vp/products/7335597976";

    private final ProductPageScrapePort scrapePort = mock(ProductPageScrapePort.class);
    private final CoupangProductPort coupangProductPort = mock(CoupangProductPort.class);
    private final SourceProductProvider provider = new SourceProductProvider(scrapePort, coupangProductPort);

    @Test
    @DisplayName("카탈로그에 있는 쿠팡 상품은 페이지를 긁지 않고 카탈로그 값으로 응답한다")
    void 카탈로그에_있으면_스크래핑하지_않는다() {
        when(coupangProductPort.findByCoupangProductId("7335597976")).thenReturn(Optional.of(new CoupangProduct(
                1L, "힘내바 초코 스니커즈, 480g, 1개", "https://thumbnail.coupangcdn.com/a.jpg",
                "https://link.coupang.com/re/AFFSDP?pageKey=7335597976", 14770L, 16400L, 10)));

        ScrapedProduct product = provider.provide(SourceUrl.normalize(COUPANG_URL));

        verify(scrapePort, never()).scrape(any());
        assertThat(product.sourceUrl()).isEqualTo(COUPANG_CANONICAL_URL);
        assertThat(product.title()).isEqualTo("힘내바 초코 스니커즈, 480g, 1개");
        assertThat(product.thumbnailUrl()).isEqualTo("https://thumbnail.coupangcdn.com/a.jpg");
        assertThat(product.price()).isEqualTo(14770L);
        assertThat(product.currency()).isEqualTo("KRW");
        assertThat(product.hasEssentials()).isTrue();
    }

    @Test
    @DisplayName("카탈로그에 없는 쿠팡 상품은 정규화된 URL 로 페이지를 긁는다")
    void 카탈로그에_없으면_스크래핑한다() {
        ScrapedProduct scraped = new ScrapedProduct(COUPANG_CANONICAL_URL, "소파", "https://cdn/a.jpg",
                null, 100000L, "KRW", List.of(), null);
        when(coupangProductPort.findByCoupangProductId("7335597976")).thenReturn(Optional.empty());
        when(scrapePort.scrape(SourceUrl.normalize(COUPANG_URL))).thenReturn(scraped);

        assertThat(provider.provide(SourceUrl.normalize(COUPANG_URL))).isEqualTo(scraped);
    }

    @Test
    @DisplayName("카탈로그 상품에 이미지가 없으면 필수값을 채우려고 페이지를 긁는다")
    void 카탈로그_값이_불완전하면_스크래핑한다() {
        ScrapedProduct scraped = new ScrapedProduct(COUPANG_CANONICAL_URL, "소파", "https://cdn/a.jpg",
                null, 100000L, "KRW", List.of(), null);
        when(coupangProductPort.findByCoupangProductId("7335597976")).thenReturn(Optional.of(new CoupangProduct(
                1L, "소파", null, "https://link.coupang.com/re/x", 100000L, 100000L, 0)));
        when(scrapePort.scrape(any())).thenReturn(scraped);

        assertThat(provider.provide(SourceUrl.normalize(COUPANG_URL))).isEqualTo(scraped);
    }

    @Test
    @DisplayName("쿠팡이 아닌 몰은 카탈로그를 조회하지 않는다")
    void 쿠팡이_아니면_카탈로그를_보지_않는다() {
        SourceUrl ohou = SourceUrl.normalize("https://ohou.se/productions/123");
        when(scrapePort.scrape(ohou)).thenReturn(ScrapedProduct.empty(ohou.value()));

        provider.provide(ohou);

        verify(coupangProductPort, never()).findByCoupangProductId(anyString());
        verify(scrapePort).scrape(ohou);
    }
}
