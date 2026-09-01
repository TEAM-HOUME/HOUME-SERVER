package or.sopt.houme.domain.coupang.client;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.coupang.config.CoupangPartnersProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

@FeignClient(
        name = "coupangPartnersFeignClient",
        url = "${coupang.partners.base-url:https://api-gateway.coupang.com}",
        configuration = CoupangPartnersFeignClient.Configuration.class
)
public interface CoupangPartnersFeignClient {

    String SEARCH_PATH = "/v2/providers/affiliate_open_api/apis/openapi/v1/products/search";

    @GetMapping(SEARCH_PATH)
    String searchProducts(
            @RequestParam("keyword") String keyword,
            @RequestParam("limit") int limit
    );

    @RequiredArgsConstructor
    class Configuration {

        private static final DateTimeFormatter SIGNED_DATE_FORMATTER =
                DateTimeFormatter.ofPattern("yyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

        private final CoupangPartnersProperties properties;

        @Bean
        RequestInterceptor coupangPartnersAuthorizationInterceptor() {
            return template -> {
                if (!properties.isConfigured()) {
                    throw new CoupangPartnersClientException("쿠팡 파트너스 API 키가 설정되지 않았습니다.");
                }

                String signedDate = SIGNED_DATE_FORMATTER.format(Clock.systemUTC().instant());
                String signature = createSignature(signatureInput(signedDate, template.method(), template.url()));
                String authorization = "CEA algorithm=HmacSHA256, access-key=" + properties.getAccessKey()
                        + ", signed-date=" + signedDate + ", signature=" + signature;

                template.header("Authorization", authorization);
                template.header("Content-Type", "application/json");
            };
        }

        static String signatureInput(String signedDate, String method, String requestUrl) {
            int queryStartIndex = requestUrl.indexOf('?');
            if (queryStartIndex < 0) {
                return signedDate + method + requestUrl;
            }

            String path = requestUrl.substring(0, queryStartIndex);
            String query = requestUrl.substring(queryStartIndex + 1);
            return signedDate + method + path + query;
        }

        private String createSignature(String message) {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(properties.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                throw new CoupangPartnersClientException("쿠팡 파트너스 API 서명 생성에 실패했습니다.", e);
            }
        }
    }
}
