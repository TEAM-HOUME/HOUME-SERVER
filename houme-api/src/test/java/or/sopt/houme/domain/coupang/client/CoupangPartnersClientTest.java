package or.sopt.houme.domain.coupang.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import or.sopt.houme.domain.coupang.config.CoupangPartnersProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoupangPartnersClientTest {

    private final CoupangPartnersClient client = new CoupangPartnersClient(
            new CoupangPartnersProperties(),
            new ObjectMapper()
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
}
