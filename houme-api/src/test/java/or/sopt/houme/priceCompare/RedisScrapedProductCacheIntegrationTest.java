package or.sopt.houme.priceCompare;

import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.ScrapedProductCachePort;
import or.sopt.houme.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("스크래핑 결과 Redis 캐시")
class RedisScrapedProductCacheIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ScrapedProductCachePort scrapedProductCachePort;

    @Test
    @DisplayName("저장한 스크래핑 결과를 같은 URL 로 그대로 다시 읽는다")
    void 저장한_결과를_다시_읽는다() {
        SourceUrl url = uniqueCoupangUrl();
        ScrapedProduct product = new ScrapedProduct(url.value(), "힘내바 초코 스니커즈, 480g, 1개",
                "https://thumbnail.coupangcdn.com/a.jpg", null, 14770L, "KRW",
                List.of("https://thumbnail.coupangcdn.com/b.jpg"), "현재 별점 4.6점");

        scrapedProductCachePort.save(product);

        assertThat(scrapedProductCachePort.find(url)).contains(product);
    }

    @Test
    @DisplayName("저장한 적 없는 URL 은 캐시 미스다")
    void 저장한_적_없으면_비어있다() {
        assertThat(scrapedProductCachePort.find(uniqueCoupangUrl())).isEmpty();
    }

    private static SourceUrl uniqueCoupangUrl() {
        long productId = Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000_000L);
        return SourceUrl.normalize("https://www.coupang.com/vp/products/" + productId);
    }
}
