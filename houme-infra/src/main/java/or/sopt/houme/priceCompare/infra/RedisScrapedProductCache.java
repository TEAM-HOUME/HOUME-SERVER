package or.sopt.houme.priceCompare.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.ScrapedProductCachePort;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 스크래핑 결과 캐시.
 *
 * <p>같은 상품 페이지를 반복해서 긁지 않으려는 용도다. 쿠팡은 짧은 시간 연속 요청에 서버 IP 를 막으므로
 * 재요청 자체를 줄이는 게 차단을 피하는 가장 확실한 방법이다.
 * TTL 은 가격 변동을 감안해 반나절 이내로 둔다.
 *
 * <p>캐시는 부가 기능이라 Redis 장애나 직렬화 실패가 요청 실패로 번지지 않게 삼키고 캐시 미스로 취급한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisScrapedProductCache implements ScrapedProductCachePort {

    private static final String KEY_PREFIX = "scraped-product:";
    private static final Duration TTL = Duration.ofHours(6);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ScrapedProduct> find(SourceUrl sourceUrl) {
        try {
            String json = stringRedisTemplate.opsForValue().get(KEY_PREFIX + sourceUrl.value());
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, Dto.class).toDomain());
        } catch (JsonProcessingException | DataAccessException e) {
            log.warn("스크래핑 캐시 조회 실패: url={}, message={}", sourceUrl.value(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void save(ScrapedProduct product) {
        try {
            String json = objectMapper.writeValueAsString(Dto.from(product));
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + product.sourceUrl(), json, TTL);
        } catch (JsonProcessingException | DataAccessException e) {
            log.warn("스크래핑 캐시 저장 실패: url={}, message={}", product.sourceUrl(), e.getMessage());
        }
    }

    private record Dto(
            String sourceUrl,
            String title,
            String thumbnailUrl,
            String brand,
            Long price,
            String currency,
            List<String> additionalImageUrls,
            String description
    ) {
        static Dto from(ScrapedProduct product) {
            return new Dto(
                    product.sourceUrl(),
                    product.title(),
                    product.thumbnailUrl(),
                    product.brand(),
                    product.price(),
                    product.currency(),
                    product.additionalImageUrls(),
                    product.description()
            );
        }

        ScrapedProduct toDomain() {
            return new ScrapedProduct(
                    sourceUrl, title, thumbnailUrl, brand, price, currency, additionalImageUrls, description);
        }
    }
}
