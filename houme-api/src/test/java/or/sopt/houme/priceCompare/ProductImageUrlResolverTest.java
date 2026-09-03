package or.sopt.houme.priceCompare;

import or.sopt.houme.priceCompare.external.scrape.ProductImageUrlResolver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("상품 이미지 URL 해석")
class ProductImageUrlResolverTest {

    private static final String BASE_URI = "https://example-mall.co.kr/products/123";

    private final ProductImageUrlResolver resolver = new ProductImageUrlResolver();

    @Test
    @DisplayName("도메인이 빠진 상대경로를 절대 URL 로 바꾼다")
    void 상대경로를_절대_URL_로_바꾼다() {
        assertThat(resolver.resolve("/images/a.jpg", BASE_URI))
                .contains("https://example-mall.co.kr/images/a.jpg");
    }

    @Test
    @DisplayName("스킴이 빠진 프로토콜 상대 URL 에 https 를 붙인다")
    void 프로토콜_상대_URL_에_https_를_붙인다() {
        assertThat(resolver.resolve("//cdn.example-mall.co.kr/a.jpg", BASE_URI))
                .contains("https://cdn.example-mall.co.kr/a.jpg");
    }

    @Test
    @DisplayName("이미 절대 URL 이면 그대로 둔다")
    void 절대_URL_은_그대로_둔다() {
        assertThat(resolver.resolve("https://cdn.other.com/a.jpg", BASE_URI))
                .contains("https://cdn.other.com/a.jpg");
    }

    @Test
    @DisplayName("인라인 데이터 URI 는 상품 이미지로 보지 않는다")
    void 데이터_URI_는_제외한다() {
        assertThat(resolver.resolve("data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==",
                BASE_URI)).isEmpty();
    }

    @Test
    @DisplayName("플레이스홀더로 보이는 주소는 제외한다")
    void 플레이스홀더_이미지는_제외한다() {
        assertThat(resolver.resolve("/images/common/blank.gif", BASE_URI)).isEmpty();
        assertThat(resolver.resolve("/images/noimage.png", BASE_URI)).isEmpty();
    }

    @Test
    @DisplayName("srcset 이 있으면 가장 큰 해상도를 고른다")
    void srcset_에서_가장_큰_해상도를_고른다() {
        Element image = imageElement(
                "<img src='/images/small.jpg' srcset='/images/small.jpg 1x, /images/large.jpg 2x'>");

        assertThat(resolver.resolveFromElement(image, BASE_URI))
                .contains("https://example-mall.co.kr/images/large.jpg");
    }

    @Test
    @DisplayName("src 가 플레이스홀더면 lazy-load 속성에 숨겨진 실제 주소를 쓴다")
    void lazy_load_속성에서_실제_주소를_찾는다() {
        Element image = imageElement(
                "<img src='data:image/gif;base64,AAAA' data-src='/images/products/real.jpg'>");

        assertThat(resolver.resolveFromElement(image, BASE_URI))
                .contains("https://example-mall.co.kr/images/products/real.jpg");
    }

    private Element imageElement(String html) {
        return Jsoup.parse(html, BASE_URI).selectFirst("img");
    }
}
