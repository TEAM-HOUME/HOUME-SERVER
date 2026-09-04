package or.sopt.houme.priceCompare;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;
import or.sopt.houme.priceCompare.application.ProductScrapeService;
import or.sopt.houme.priceCompare.application.dto.ScrapedProductResponse;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.ProductPageScrapePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("상품 URL 메타데이터 추출 유스케이스")
class ProductScrapeServiceTest {

    private final ProductPageScrapePort scrapePort = mock(ProductPageScrapePort.class);
    private final ProductScrapeService productScrapeService = new ProductScrapeService(scrapePort);

    @Test
    @DisplayName("정규화된 URL 로 스크래핑을 요청하고 결과를 응답으로 변환한다")
    void 정규화된_URL_로_스크래핑한다() {
        when(scrapePort.scrape(any())).thenReturn(new ScrapedProduct(
                "https://ohou.se/productions/123", "패브릭 소파", "https://cdn.ohou.se/a.jpg",
                "에싸", 459000L, "KRW", List.of(), null));

        ScrapedProductResponse response = productScrapeService.scrape("ohou.se/productions/123?utm_source=kakao");

        ArgumentCaptor<SourceUrl> captor = ArgumentCaptor.forClass(SourceUrl.class);
        verify(scrapePort).scrape(captor.capture());
        assertThat(captor.getValue().value()).isEqualTo("https://ohou.se/productions/123");

        assertThat(response.title()).isEqualTo("패브릭 소파");
        assertThat(response.price()).isEqualTo(459000L);
        assertThat(response.quality()).isEqualTo("FULL");
    }

    @Test
    @DisplayName("가격이 있고 상품명·이미지 중 하나만 있으면 부분 성공으로 통과시킨다")
    void 부분_성공은_실패로_보지_않는다() {
        when(scrapePort.scrape(any())).thenReturn(new ScrapedProduct(
                "https://ohou.se/productions/123", "패브릭 소파", null,
                null, 459000L, "KRW", List.of(), null));

        ScrapedProductResponse response = productScrapeService.scrape("https://ohou.se/productions/123");

        assertThat(response.title()).isEqualTo("패브릭 소파");
        assertThat(response.quality()).isEqualTo("MINIMAL");
    }

    @Test
    @DisplayName("상품명·이미지가 있어도 가격이 없으면 추출 실패로 처리한다")
    void 가격이_없으면_실패로_처리한다() {
        when(scrapePort.scrape(any())).thenReturn(new ScrapedProduct(
                "https://ohou.se/productions/123", "패브릭 소파", "https://cdn.ohou.se/a.jpg",
                "에싸", null, "KRW", List.of(), null));

        assertThatThrownBy(() -> productScrapeService.scrape("https://ohou.se/productions/123"))
                .isInstanceOf(PriceCompareException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_METADATA_PARSE_FAILED);
    }

    @Test
    @DisplayName("가격이 0원이면 가격을 못 뽑은 것과 같게 실패로 처리한다")
    void 가격이_0원이면_실패로_처리한다() {
        when(scrapePort.scrape(any())).thenReturn(new ScrapedProduct(
                "https://ohou.se/productions/123", "패브릭 소파", "https://cdn.ohou.se/a.jpg",
                "에싸", 0L, "KRW", List.of(), null));

        assertThatThrownBy(() -> productScrapeService.scrape("https://ohou.se/productions/123"))
                .isInstanceOf(PriceCompareException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_METADATA_PARSE_FAILED);
    }

    @Test
    @DisplayName("앞선 파서의 0원은 뒤 파서의 실제 가격을 막지 않는다")
    void 앞선_파서의_0원은_실제_가격을_막지_않는다() {
        ScrapedProduct zeroPrice = new ScrapedProduct(
                "https://ohou.se/productions/123", "패브릭 소파", null, null, 0L, "KRW", List.of(), null);
        ScrapedProduct next = new ScrapedProduct(
                "https://ohou.se/productions/123", null, null, "에싸", 459000L, "KRW", List.of(), null);

        ScrapedProduct merged = zeroPrice.fillMissingFrom(next);

        assertThat(merged.price()).isEqualTo(459000L);
        assertThat(merged.hasEssentials()).isTrue();
    }

    @Test
    @DisplayName("상품명도 이미지도 못 건지면 추출 실패로 처리한다")
    void 필수_정보가_없으면_실패로_처리한다() {
        when(scrapePort.scrape(any()))
                .thenReturn(ScrapedProduct.empty("https://ohou.se/productions/123"));

        assertThatThrownBy(() -> productScrapeService.scrape("https://ohou.se/productions/123"))
                .isInstanceOf(PriceCompareException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_METADATA_PARSE_FAILED);
    }
}
