package or.sopt.houme.domain.furniture.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * #618 회귀 테스트 — 캐시 값이 RedisConfig와 동일한 직렬화기
 * ({@link GenericJackson2JsonRedisSerializer})로 저장·복원 가능한지 검증한다.
 *
 * 기존 테스트는 FurnitureMasterCacheService를 mock으로 대체해 실제 직렬화 경로를
 * 한 번도 타지 않았고, 엔티티(순환 참조)를 캐시에 넣는 버그가 CI를 통과했다.
 * 이 테스트는 캐시에 들어가는 실제 타입이 직렬화 왕복(round-trip)을 통과함을 보장한다.
 */
class FurnitureMasterCacheSerializationTest {

    private final GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

    @Test
    @DisplayName("가구 캐시 읽기 모델 리스트는 Redis JSON 직렬화 왕복이 가능하다")
    void furnitureCacheViewListRoundTrip() {
        List<FurnitureCacheView> original = Stream.of(
                new FurnitureCacheView(1L, "OFFICE_DESK"),
                new FurnitureCacheView(2L, "DINING_TABLE"),
                new FurnitureCacheView(3L, null) // nameEng이 null인 행도 존재할 수 있다
        ).collect(Collectors.toList());

        byte[] bytes = serializer.serialize(original);
        Object restored = serializer.deserialize(bytes);

        assertThat(restored).isInstanceOf(List.class);
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
        // 캐시 히트 시 소비처가 record accessor로 바로 읽을 수 있어야 한다 (LinkedHashMap 강등 금지)
        FurnitureCacheView first = ((List<FurnitureCacheView>) restored).get(0);
        assertThat(first.id()).isEqualTo(1L);
        assertThat(first.furnitureNameEng()).isEqualTo("OFFICE_DESK");
    }

    @Test
    @DisplayName("가구 타입 캐시 읽기 모델 리스트는 Redis JSON 직렬화 왕복이 가능하다")
    void furnitureTypeCacheViewListRoundTrip() {
        List<FurnitureTypeCacheView> original = new ArrayList<>(List.of(
                new FurnitureTypeCacheView(1L, "BED"),
                new FurnitureTypeCacheView(2L, "SOFA"),
                new FurnitureTypeCacheView(3L, "ETC")
        ));

        byte[] bytes = serializer.serialize(original);
        Object restored = serializer.deserialize(bytes);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
        FurnitureTypeCacheView first = ((List<FurnitureTypeCacheView>) restored).get(0);
        assertThat(first.id()).isEqualTo(1L);
        assertThat(first.nameEng()).isEqualTo("BED");
    }
}
