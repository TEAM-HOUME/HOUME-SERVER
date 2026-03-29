package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = {
    "IMAGE_BASE_URL=http://localhost",
    "SOOZIP_BASE_URL=http://localhost",
    "GEMINI_API_BASE_URL=http://localhost",
    "JWT_SECRET=dummy_secret_dummy_secret_dummy_secret_dummy_secret_dummy_secret",
    "KAKAO_CLIENT_ID=dummy",
    "OPEN_AI_KEY=dummy",
    "GEMINI_API_KEY=dummy",
    "NAVER_CLIENT_ID=dummy",
    "NAVER_CLIENT_SECRET=dummy",
    "NAVER_ALLOWED_MALLS=dummy",
    "DISCORD_WEBHOOK=http://localhost",
    "S3_ACCESS=dummy",
    "S3_SECRET=dummy",
    "BUCKET_DOMAIN=dummy",
    "LOCAL_DB_URL=jdbc:postgresql://localhost:5432/houme_local",
    "LOCAL_DB_USERNAME=postgres",
    "LOCAL_DB_PASSWORD=dla15951"
})
@Transactional
@DisplayName("상품 큐레이션 서비스 통합 테스트")
class CurationProductServiceImplIntegrationTest {

    @Autowired
    private CurationProductService curationProductService;

    @Test
    @DisplayName("getFilterMetadata()가 실제 DB 데이터와 정적 필터를 조합하여 반환한다")
    void getFilterMetadata() {
        // when
        CurationProductFilterResponse response = curationProductService.getFilterMetadata();

        // then
        assertThat(response).isNotNull();
        
        System.out.println("=== [가구 유형 필터 리스트] ===");
        response.furnitureTypes().forEach(ft -> 
            System.out.println("ID: " + ft.id() + ", 한글명: " + ft.nameKr() + ", 영문명: " + ft.nameEng())
        );

        System.out.println("\n=== [가격대 필터 리스트] ===");
        response.priceRanges().forEach(pr -> 
            System.out.println("ID: " + pr.id() + ", 라벨: " + pr.label() + ", 범위: " + pr.min() + " ~ " + pr.max())
        );

        System.out.println("\n=== [색상 필터 리스트] ===");
        response.colors().forEach(c -> 
            System.out.println("ID: " + c.id() + ", 색상명: " + c.label() + ", Hex: " + c.value())
        );

        // 검증: 침대(1번)가 포함되어 있는지
        boolean hasBed = response.furnitureTypes().stream()
                .anyMatch(ft -> ft.id().equals(1L) && ft.nameKr().equals("침대/프레임"));
        assertThat(hasBed).isTrue();

        // 검증: 가격대 P1(5만원 이하)이 포함되어 있는지
        boolean hasP1 = response.priceRanges().stream()
                .anyMatch(pr -> pr.id().equals("P1") && pr.min().equals(0L));
        assertThat(hasP1).isTrue();

        // 검증: 색상 블랙(10번)이 포함되어 있는지
        boolean hasBlack = response.colors().stream()
                .anyMatch(c -> c.id().equals(10L) && c.label().equals("블랙"));
        assertThat(hasBlack).isTrue();
    }
}
