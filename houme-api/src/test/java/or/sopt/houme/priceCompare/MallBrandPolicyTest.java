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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 몰 브랜드 정책 검증.
 *
 * <p>입점몰의 브랜드 칸을 몰 이름으로 통일하되,
 * 자사 브랜드를 파는 몰의 실제 브랜드는 덮지 않는다는 것이 핵심이다.
 */
@DisplayName("몰 브랜드 정책")
class MallBrandPolicyTest {

    private static final String SOOZIP_URL =
            "https://soozip.co.kr/product/detail.html?product_no=833&cate_no=75&display_group=1";
    private static final String IKEA_URL =
            "https://www.ikea.com/kr/ko/p/glostad-2-seat-sofa-knisa-dark-grey-10489009/";

    private final MallBrandPolicy policy = new MallBrandPolicy();

    @Test
    @DisplayName("수집은 브랜드가 비어 있으면 몰 이름으로 채운다")
    void 빈_브랜드를_몰_이름으로_채운다() {
        Document document = load("scrape/soozip-product.html", SOOZIP_URL);
        ScrapedProduct parsed = product(null, SOOZIP_URL);

        ScrapedProduct result = policy.apply(parsed, document, SOOZIP_URL);

        assertThat(result.brand()).isEqualTo("수집");
    }

    @Test
    @DisplayName("몰이 제 이름을 넣어둔 경우 표기를 몰 이름으로 통일한다")
    void 몰이_넣은_제_이름을_통일한다() {
        Document document = load("scrape/soozip-product.html", SOOZIP_URL);
        ScrapedProduct parsed = product("SOOZIP 수집", SOOZIP_URL);

        ScrapedProduct result = policy.apply(parsed, document, SOOZIP_URL);

        assertThat(result.brand()).isEqualTo("수집");
    }

    @Test
    @DisplayName("브랜드를 채워도 나머지 필드는 건드리지 않는다")
    void 브랜드_외의_필드는_유지한다() {
        Document document = load("scrape/soozip-product.html", SOOZIP_URL);
        ScrapedProduct parsed = product(null, SOOZIP_URL);

        ScrapedProduct result = policy.apply(parsed, document, SOOZIP_URL);

        assertThat(result.title()).isEqualTo(parsed.title());
        assertThat(result.price()).isEqualTo(parsed.price());
        assertThat(result.thumbnailUrl()).isEqualTo(parsed.thumbnailUrl());
    }

    @Test
    @DisplayName("대상 몰이라도 실제 브랜드가 들어오면 덮지 않는다")
    void 실제_브랜드는_덮지_않는다() {
        Document document = load("scrape/soozip-product.html", SOOZIP_URL);
        ScrapedProduct parsed = product("리샘", SOOZIP_URL);

        ScrapedProduct result = policy.apply(parsed, document, SOOZIP_URL);

        assertThat(result.brand()).isEqualTo("리샘");
    }

    @Test
    @DisplayName("대상이 아닌 몰은 건드리지 않는다 - 이케아의 IKEA 를 유지한다")
    void 자사_브랜드_몰은_건드리지_않는다() {
        Document document = load("scrape/ikea-product.html", IKEA_URL);
        ScrapedProduct parsed = product("IKEA", IKEA_URL);

        ScrapedProduct result = policy.apply(parsed, document, IKEA_URL);

        assertThat(result.brand()).isEqualTo("IKEA");
    }

    @Test
    @DisplayName("체인 전체에서 수집은 브랜드가 채워져 FULL 이 된다")
    void 체인에서_수집은_FULL_이_된다() {
        ProductImageUrlResolver imageUrlResolver = new ProductImageUrlResolver();
        PriceTextParser priceTextParser = new PriceTextParser();
        List<ProductPageParser> parsers = List.of(
                new JsonLdProductParser(new ObjectMapper(), imageUrlResolver, priceTextParser),
                new OpenGraphProductParser(imageUrlResolver, priceTextParser),
                new OhouProductParser(new ObjectMapper()),
                new FallbackProductParser(imageUrlResolver)
        );
        ProductPageFetcher fetcher = mock(ProductPageFetcher.class);
        when(fetcher.fetch(anyString())).thenReturn(load("scrape/soozip-product.html", SOOZIP_URL));

        ScrapedProduct result = new ProductPageScrapeAdapter(fetcher, parsers, new MallBrandPolicy())
                .scrape(new SourceUrl(SOOZIP_URL));

        assertThat(result.brand()).isEqualTo("수집");
        assertThat(result.title()).isEqualTo("리샘 모엘로 2인 3인 스틸 다리 가죽소파");
        assertThat(result.price()).isEqualTo(419900L);
        assertThat(result.quality()).isEqualTo(ScrapeQuality.FULL);
    }

    private ScrapedProduct product(String brand, String sourceUrl) {
        return new ScrapedProduct(
                sourceUrl, "리샘 모엘로 2인 3인 스틸 다리 가죽소파",
                "https://cdn.example.invalid/main.jpg", brand, 419900L, "KRW", List.of(), null);
    }

    private Document load(String path, String baseUri) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("픽스처를 찾을 수 없습니다: " + path);
            }
            return Jsoup.parse(in, "UTF-8", baseUri);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
