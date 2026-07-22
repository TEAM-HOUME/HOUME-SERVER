package or.sopt.houme.support;

import or.sopt.houme.domain.furniture.infrastructure.client.FastApiImageHashClient;
import or.sopt.houme.domain.furniture.infrastructure.client.NaverShopApiClient;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.client.GeminiImageClient;
import or.sopt.houme.domain.generateImage.infrastructure.openai.client.FastApiImageClient;
import or.sopt.houme.domain.generateImage.infrastructure.openai.client.OpenAIImageClient;
import or.sopt.houme.domain.user.infrastructure.client.KaKaoOAuthClient;
import or.sopt.houme.domain.user.infrastructure.client.KaKaoUserInfoClient;
import or.sopt.houme.global.util.S3Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * API 계약(통합) 테스트 공통 베이스.
 *
 * <p>실제 PostgreSQL 16 / Redis 7 컨테이너 위에서 전체 스프링 컨텍스트를 띄운다
 * (컨테이너 공급은 {@link TestContainersInitializer} — application-test.yml 로 전역 등록).
 * 운영과 동일한 DB(jsonb, pg_trgm)를 사용하므로 H2 방언 차이로 인한 오탐/미탐이 없다.
 *
 * <p>외부 시스템 경계(Feign 클라이언트, S3)만 {@link MockBean}으로 대체한다.
 * 서비스/파사드/레포지토리 등 내부 컴포넌트는 절대 목으로 대체하지 않는다 —
 * 이 테스트들의 목적은 구현 구조가 아니라 API 행동 계약을 고정하는 것이다(#580).
 * 시나리오가 필요한 테스트는 protected 목 필드를 {@code given(...)}으로 스텁한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    // ===== 외부 시스템 경계 목 (네트워크/AWS 경계만 대체) =====

    @MockBean
    protected GeminiImageClient geminiImageClient;

    @MockBean
    protected OpenAIImageClient openAiImageClient;

    @MockBean
    protected FastApiImageClient fastApiImageClient;

    @MockBean
    protected FastApiImageHashClient fastApiImageHashClient;

    @MockBean
    protected KaKaoOAuthClient kakaoOAuthClient;

    @MockBean
    protected KaKaoUserInfoClient kakaoUserInfoClient;

    @MockBean
    protected NaverShopApiClient naverShopApiClient;

    @MockBean
    protected S3Util s3Util;
}
