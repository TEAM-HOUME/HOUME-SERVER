package or.sopt.houme.priceCompare;

import com.fasterxml.jackson.databind.ObjectMapper;
import or.sopt.houme.priceCompare.domain.ScrapeQuality;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.external.scrape.FallbackProductParser;
import or.sopt.houme.priceCompare.external.scrape.JsonLdProductParser;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 파서 체인의 핵심 동작 검증.
 *
 * <p>네트워크에 나가지 않도록 fetcher 를 목으로 두고, 실제 몰 HTML을 본뜬 고정 픽스처를 먹인다.
 * CI 가 외부 사이트 상태에 좌우되면 안 되기 때문이다.
 */
@DisplayName("상품 페이지 스크래핑 파서 체인")
class ProductPageScrapeAdapterTest {

    private static final String SOURCE_URL = "https://example-mall.co.kr/products/123";

    private ProductPageFetcher productPageFetcher;
    private ProductPageScrapeAdapter adapter;

    @BeforeEach
    void setUp() {
        ProductImageUrlResolver imageUrlResolver = new ProductImageUrlResolver();
        PriceTextParser priceTextParser = new PriceTextParser();
        List<ProductPageParser> parsers = List.of(
                new JsonLdProductParser(new ObjectMapper(), imageUrlResolver, priceTextParser),
                new OpenGraphProductParser(imageUrlResolver, priceTextParser),
                new FallbackProductParser(imageUrlResolver)
        );

        productPageFetcher = mock(ProductPageFetcher.class);
        adapter = new ProductPageScrapeAdapter(productPageFetcher, parsers);
    }

    @Test
    @DisplayName("JSON-LD 가 있으면 OG 태그보다 우선해서 채택한다")
    void JSON_LD_를_OG_보다_우선한다() {
        ScrapedProduct product = scrape("scrape/jsonld-product.html");

        assertThat(product.title()).isEqualTo("에스테 3인용 패브릭 소파");
        assertThat(product.thumbnailUrl()).isEqualTo("https://example-mall.co.kr/images/products/sofa-main.jpg");
        assertThat(product.brand()).isEqualTo("에싸");
        assertThat(product.price()).isEqualTo(459000L);
        assertThat(product.currency()).isEqualTo("KRW");
        assertThat(product.additionalImageUrls())
                .contains("https://cdn.example-mall.co.kr/images/sofa-detail-1.jpg");
        assertThat(product.quality()).isEqualTo(ScrapeQuality.FULL);
    }

    @Test
    @DisplayName("JSON-LD 가 없는 페이지는 OG 태그만으로 필수 필드를 채운다")
    void OG_태그만으로_필수_필드를_채운다() {
        ScrapedProduct product = scrape("scrape/og-product.html");

        assertThat(product.title()).isEqualTo("모던 원목 4인 식탁");
        assertThat(product.thumbnailUrl()).isEqualTo("https://example-mall.co.kr/images/products/table-main.jpg");
        assertThat(product.brand()).isEqualTo("우드하우스");
        assertThat(product.price()).isEqualTo(329000L);
        assertThat(product.quality()).isEqualTo(ScrapeQuality.FULL);
    }

    @Test
    @DisplayName("앞선 파서가 채운 값은 유지하고 비어 있는 칸만 다음 파서가 보충한다")
    void 앞선_파서의_값을_덮어쓰지_않는다() {
        ScrapedProduct product = scrape("scrape/partial-product.html");

        // 이름·가격은 JSON-LD 가 이미 채웠으므로 OG 값("라운지 체어 (OG)")이 덮어쓰면 안 된다
        assertThat(product.title()).isEqualTo("라운지 체어 (JSON-LD)");
        assertThat(product.price()).isEqualTo(189000L);
        // 이미지·브랜드는 JSON-LD 에 없으므로 OG 가 채워야 한다
        assertThat(product.thumbnailUrl()).isEqualTo("https://cdn.example-mall.co.kr/images/chair-og.jpg");
        assertThat(product.brand()).isEqualTo("체어랩");
    }

    @Test
    @DisplayName("규격 메타데이터가 없으면 title 과 본문 이미지로 최소 정보를 건진다")
    void 메타데이터가_없으면_최소_정보를_건진다() {
        ScrapedProduct product = scrape("scrape/bare-product.html");

        assertThat(product.title()).isEqualTo("수납형 침대 프레임 슈퍼싱글 - 예시몰");
        // 플레이스홀더(data URI, blank.gif)와 아이콘(24x24)을 걸러내고 상품 이미지를 골라야 한다
        assertThat(product.thumbnailUrl()).isEqualTo("https://example-mall.co.kr/images/products/bed-main.jpg");
        assertThat(product.quality()).isEqualTo(ScrapeQuality.PARTIAL);
        // 폴백 파서는 가격을 뽑지 못하고, 가격 없이는 비교 필터를 태울 수 없다
        assertThat(product.hasEssentials()).isFalse();
    }

    @Test
    @DisplayName("크기가 명시된 이미지가 모두 기준 미달이면 추적 픽셀을 썸네일로 쓰지 않는다")
    void 추적_픽셀을_썸네일로_쓰지_않는다() {
        ScrapedProduct product = scrape("scrape/tracking-pixel-product.html");

        assertThat(product.title()).isEqualTo("원목 사이드 테이블 - 예시몰");
        assertThat(product.thumbnailUrl()).isNull();
    }

    private ScrapedProduct scrape(String fixturePath) {
        when(productPageFetcher.fetch(anyString())).thenReturn(loadFixture(fixturePath));
        return adapter.scrape(new SourceUrl(SOURCE_URL));
    }

    private Document loadFixture(String path) {
        try (InputStream fixture = getClass().getClassLoader().getResourceAsStream(path)) {
            if (fixture == null) {
                throw new IllegalStateException("픽스처를 찾을 수 없습니다: " + path);
            }
            return Jsoup.parse(fixture, "UTF-8", SOURCE_URL);
        } catch (IOException e) {
            throw new IllegalStateException("픽스처 로딩 실패: " + path, e);
        }
    }
}
