package or.sopt.houme.priceCompare;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;
import or.sopt.houme.priceCompare.external.scrape.SourceUrlValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SSRF 방어 검증.
 *
 * <p>유저가 URL을 자유롭게 넣는 기능이라, 막지 않으면 서버가 사내망이나
 * 클라우드 메타데이터 엔드포인트를 대신 긁어다 주는 통로가 된다.
 * 외부 DNS 에 의존하지 않도록 IP 리터럴·로컬 호스트명만 사용한다.
 */
@DisplayName("스크래핑 대상 주소 검증(SSRF 방어)")
class SourceUrlValidatorTest {

    private final SourceUrlValidator validator = new SourceUrlValidator();

    @ParameterizedTest(name = "[{index}] {0}")
    @DisplayName("내부망으로 향하는 주소는 차단한다")
    @ValueSource(strings = {
            "http://127.0.0.1/admin",
            "http://localhost/admin",
            "http://169.254.169.254/latest/meta-data/",  // 클라우드 인스턴스 메타데이터
            "http://10.0.0.5/internal",
            "http://192.168.0.1/router",
            "http://172.16.0.1/internal",
            "http://[::1]/admin",
            "http://[fd00::1]/internal",  // IPv6 ULA — isSiteLocalAddress() 로는 잡히지 않는다
            "http://[fc00::1]/internal"
    })
    void 내부망_주소는_차단한다(String url) {
        assertThatThrownBy(() -> validator.validate(URI.create(url)))
                .isInstanceOf(PriceCompareException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_PRODUCT_URL);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @DisplayName("http/https 가 아닌 스킴은 차단한다")
    @ValueSource(strings = {"file:///etc/passwd", "ftp://example.com/a", "gopher://example.com/"})
    void 허용되지_않은_스킴은_차단한다(String url) {
        assertThatThrownBy(() -> validator.validate(URI.create(url)))
                .isInstanceOf(PriceCompareException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_PRODUCT_URL);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @DisplayName("80/443 이 아닌 포트는 차단한다")
    @ValueSource(strings = {"http://127.0.0.1:8080/actuator", "http://127.0.0.1:6379/"})
    void 허용되지_않은_포트는_차단한다(String url) {
        assertThatThrownBy(() -> validator.validate(URI.create(url)))
                .isInstanceOf(PriceCompareException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_PRODUCT_URL);
    }
}
