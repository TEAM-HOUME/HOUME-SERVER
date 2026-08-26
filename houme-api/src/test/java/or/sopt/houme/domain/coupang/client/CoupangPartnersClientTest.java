package or.sopt.houme.domain.coupang.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import or.sopt.houme.domain.coupang.config.CoupangPartnersProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class CoupangPartnersClientTest {

    private final CoupangPartnersClient client = new CoupangPartnersClient(
            new CoupangPartnersProperties(),
            new ObjectMapper(),
            mock(CoupangPartnersFeignClient.class)
    );

    @Test
    @DisplayName("rCode가 명시적으로 0인 응답만 성공으로 처리한다")
    void acceptsOnlyExplicitZeroResponseCode() throws Exception {
        assertThat(client.parseProducts("{\"rCode\":\"0\",\"data\":{\"productData\":[]}}"))
                .isEmpty();

        assertThatThrownBy(() -> client.parseProducts("{\"data\":{\"productData\":[]}}"))
                .isInstanceOf(CoupangPartnersClientException.class);

        assertThatThrownBy(() -> client.parseProducts(
                "{\"rCode\":\"1\",\"rMessage\":\"실패\",\"data\":{\"productData\":[]}}"
        ))
                .isInstanceOf(CoupangPartnersClientException.class);
    }

    @Test
    @DisplayName("쿠팡 HMAC 서명 문자열에서는 쿼리 구분자만 제외한다")
    void excludesOnlyQueryDelimiterFromSignatureInput() {
        String signatureInput = CoupangPartnersFeignClient.Configuration.signatureInput(
                "250101T000000Z",
                "GET",
                "/products/search?keyword=%EC%86%8C%ED%8C%8C&limit=10"
        );

        assertThat(signatureInput)
                .isEqualTo("250101T000000ZGET/products/searchkeyword=%EC%86%8C%ED%8C%8C&limit=10");
    }
}
