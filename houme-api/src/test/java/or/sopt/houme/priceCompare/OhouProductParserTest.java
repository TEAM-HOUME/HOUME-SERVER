package or.sopt.houme.priceCompare;

import com.fasterxml.jackson.databind.ObjectMapper;
import or.sopt.houme.priceCompare.domain.ScrapeQuality;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.external.scrape.FallbackProductParser;
import or.sopt.houme.priceCompare.external.scrape.JsonLdProductParser;
import or.sopt.houme.priceCompare.external.scrape.MallBrandPolicy;
import or.sopt.houme.priceCompare.external.scrape.OhouProductParser;
import or.sopt.houme.priceCompare.external.scrape.OpenGraphProductParser;
import or.sopt.houme.priceCompare.external.scrape.PriceTextParser;
import or.sopt.houme.priceCompare.external.scrape.ProductImageUrlResolver;
import or.sopt.houme.priceCompare.external.scrape.ProductPageFetcher;
import or.sopt.houme.priceCompare.external.scrape.ProductPageParser;
import or.sopt.houme.priceCompare.external.scrape.ProductPageScrapeAdapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 오늘의집 전용 파서 검증.
 *
 * <p>오늘의집은 브랜드를 JSON-LD 로도 OG 로도 내려주지 않아 1·2순위만으로는 {@code brand} 가 항상 빈다.
 * 값은 {@code __NEXT_DATA__} 안에 있지만 광고·추천 상품의 브랜드가 함께 들어 있어,
 * "본 상품의 것을 골랐는가"가 이 파서의 전부다.
 */
@DisplayName("오늘의집 전용 파서")
class OhouProductParserTest {

    private static final String SOURCE_URL = "https://store.ohou.se/goods/3526970";

    private OhouProductParser parser;

    @BeforeEach
    void setUp() {
        parser = new OhouProductParser(new ObjectMapper());
    }

    @Test
    @DisplayName("본 상품의 브랜드를 뽑는다 - 광고 캐러셀의 브랜드를 집지 않는다")
    void 본_상품의_브랜드를_뽑는다() {
        Document document = load("scrape/ohou-product.html");

        Optional<ScrapedProduct> parsed = parser.parse(document, SOURCE_URL);

        assertThat(parsed).isPresent();
        assertThat(parsed.get().brand()).isEqualTo("홈비");
    }

    @Test
    @DisplayName("브랜드만 채우고 나머지 필드는 앞선 파서 몫으로 비워둔다")
    void 브랜드만_채운다() {
        Document document = load("scrape/ohou-product.html");

        ScrapedProduct parsed = parser.parse(document, SOURCE_URL).orElseThrow();

        assertThat(parsed.title()).isNull();
        assertThat(parsed.thumbnailUrl()).isNull();
        assertThat(parsed.price()).isNull();
    }

    @Test
    @DisplayName("URL 의 상품 ID 와 일치하는 노드가 없으면 아무것도 반환하지 않는다")
    void 상품_ID_가_다르면_비운다() {
        Document document = load("scrape/ohou-product.html");

        Optional<ScrapedProduct> parsed = parser.parse(document, "https://store.ohou.se/goods/9999999");

        assertThat(parsed).isEmpty();
    }

    @Test
    @DisplayName("오늘의집이 아닌 몰에서는 동작하지 않는다")
    void 다른_몰에서는_동작하지_않는다() {
        Document document = load("scrape/ohou-product.html");

        Optional<ScrapedProduct> parsed = parser.parse(document, "https://example-mall.co.kr/goods/3526970");

        assertThat(parsed).isEmpty();
    }

    @Test
    @DisplayName("__NEXT_DATA__ 가 없으면 조용히 비켜난다")
    void NEXT_DATA_가_없으면_비운다() {
        Document document = load("scrape/og-product.html");

        Optional<ScrapedProduct> parsed = parser.parse(document, SOURCE_URL);

        assertThat(parsed).isEmpty();
    }

    @Test
    @DisplayName("__NEXT_DATA__ 가 깨져 있어도 예외를 던지지 않는다")
    void 깨진_JSON_이어도_예외를_던지지_않는다() {
        Document document = Jsoup.parse(
                "<html><head><script id=\"__NEXT_DATA__\" type=\"application/json\">{ not json </script></head></html>",
                SOURCE_URL);

        Optional<ScrapedProduct> parsed = parser.parse(document, SOURCE_URL);

        assertThat(parsed).isEmpty();
    }

    @Test
    @DisplayName("체인에 얹으면 OG 가 채운 값은 그대로 두고 brand 만 보충해 PARTIAL 이 FULL 이 된다")
    void 체인에서_brand_만_보충한다() {
        ProductImageUrlResolver imageUrlResolver = new ProductImageUrlResolver();
        PriceTextParser priceTextParser = new PriceTextParser();
        List<ProductPageParser> parsers = List.of(
                new JsonLdProductParser(new ObjectMapper(), imageUrlResolver, priceTextParser),
                new OpenGraphProductParser(imageUrlResolver, priceTextParser),
                new OhouProductParser(new ObjectMapper()),
                new FallbackProductParser(imageUrlResolver)
        );
        ProductPageFetcher fetcher = mock(ProductPageFetcher.class);
        when(fetcher.fetch(anyString())).thenReturn(load("scrape/ohou-product.html"));

        ScrapedProduct product = new ProductPageScrapeAdapter(fetcher, parsers, new MallBrandPolicy())
                .scrape(new SourceUrl(SOURCE_URL));

        assertThat(product.brand()).isEqualTo("홈비");
        assertThat(product.title()).isEqualTo("브렐로 플로어 장스탠드 150 무드 램프");
        assertThat(product.thumbnailUrl())
                .isEqualTo("https://prs.ohousecdn.com/apne2/any/uploads/productions/v1-373213947351168.jpg?w=720&h=480");
        assertThat(product.quality()).isEqualTo(ScrapeQuality.FULL);
    }

    private Document load(String path) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("픽스처를 찾을 수 없습니다: " + path);
            }
            return Jsoup.parse(in, "UTF-8", SOURCE_URL);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
