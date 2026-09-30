package or.sopt.houme.priceCompare;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.external.scrape.CoupangShortLinkResolver;
import or.sopt.houme.priceCompare.external.scrape.ProductPageFetcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("쿠팡 단축 링크 해석")
class CoupangShortLinkResolverTest {

    private static final SourceUrl SHORT_LINK = SourceUrl.normalize("https://link.coupang.com/a/bQ7xyz");
    private static final URI SHORT_LINK_URI = URI.create(SHORT_LINK.value());

    private final ProductPageFetcher fetcher = mock(ProductPageFetcher.class);
    private final CoupangShortLinkResolver resolver = new CoupangShortLinkResolver(fetcher);

    @Test
    @DisplayName("리다이렉트 목적지가 쿠팡 상품 URL 이면 정규화해 돌려주고, 상품 페이지는 요청하지 않는다")
    void 상품_URL_에_닿으면_멈춘다() {
        when(fetcher.redirectLocation(SHORT_LINK_URI)).thenReturn(Optional.of(URI.create(
                "https://www.coupang.com/vp/products/194166753?itemId=12748489818&vendorItemId=88114167619&src=1139000")));

        Optional<SourceUrl> resolved = resolver.resolve(SHORT_LINK);

        assertThat(resolved).map(SourceUrl::value).contains("https://www.coupang.com/vp/products/194166753");
        verify(fetcher, times(1)).redirectLocation(any());
    }

    @Test
    @DisplayName("중간 리다이렉트를 거쳐 상품 URL 에 닿으면 거기서 멈춘다")
    void 중간_리다이렉트를_따라간다() {
        URI intermediate = URI.create("https://link.coupang.com/re/AFFSDP?lptag=AF1234567&pageKey=194166753");
        when(fetcher.redirectLocation(SHORT_LINK_URI)).thenReturn(Optional.of(intermediate));
        when(fetcher.redirectLocation(intermediate))
                .thenReturn(Optional.of(URI.create("https://www.coupang.com/vp/products/194166753?itemId=1")));

        assertThat(resolver.resolve(SHORT_LINK)).map(SourceUrl::value)
                .contains("https://www.coupang.com/vp/products/194166753");
    }

    @Test
    @DisplayName("리다이렉트가 오지 않으면 풀지 못한 것으로 본다")
    void 리다이렉트가_없으면_비어있다() {
        when(fetcher.redirectLocation(any())).thenReturn(Optional.empty());

        assertThat(resolver.resolve(SHORT_LINK)).isEmpty();
    }

    @Test
    @DisplayName("요청이 실패하면 예외를 던지지 않고 풀지 못한 것으로 본다")
    void 요청이_실패하면_비어있다() {
        when(fetcher.redirectLocation(any()))
                .thenThrow(new PriceCompareException(ErrorCode.PRODUCT_PAGE_FETCH_FAILED));

        assertThat(resolver.resolve(SHORT_LINK)).isEmpty();
    }

    @Test
    @DisplayName("상품 URL 에 닿지 못한 채 리다이렉트가 이어지면 한도에서 멈춘다")
    void 리다이렉트_한도에서_멈춘다() {
        when(fetcher.redirectLocation(any())).thenReturn(Optional.of(URI.create("https://link.coupang.com/a/loop")));

        assertThat(resolver.resolve(SHORT_LINK)).isEmpty();
        verify(fetcher, times(3)).redirectLocation(any());
    }
}
