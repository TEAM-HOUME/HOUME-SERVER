package or.sopt.houme.domain.coupang.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.domain.coupang.config.CoupangPartnersProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/** 쿠팡 파트너스 검색 API의 HMAC 인증과 응답 변환을 담당합니다. */
@Component
@RequiredArgsConstructor
public class CoupangPartnersClient {

    private static final String SEARCH_PATH = "/v2/providers/affiliate_open_api/apis/openapi/v1/products/search";
    private static final DateTimeFormatter SIGNED_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final CoupangPartnersProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock = Clock.systemUTC();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public List<CoupangProductSearchResult> searchProducts(String keyword, int limit) {
        if (!properties.isConfigured()) {
            throw new CoupangPartnersClientException("쿠팡 파트너스 API 키가 설정되지 않았습니다.");
        }

        String query = "keyword=" + encode(keyword) + "&limit=" + limit;
        String signedDate = SIGNED_DATE_FORMATTER.format(clock.instant());
        String signature = createSignature(signedDate + "GET" + SEARCH_PATH + query);
        String authorization = "CEA algorithm=HmacSHA256, access-key=" + properties.getAccessKey()
                + ", signed-date=" + signedDate + ", signature=" + signature;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl() + SEARCH_PATH + "?" + query))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new CoupangPartnersClientException("쿠팡 상품 검색 호출 실패: HTTP " + response.statusCode());
            }
            return parseProducts(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CoupangPartnersClientException("쿠팡 상품 검색 호출이 중단되었습니다.", e);
        } catch (CoupangPartnersClientException e) {
            throw e;
        } catch (Exception e) {
            throw new CoupangPartnersClientException("쿠팡 상품 검색 호출에 실패했습니다.", e);
        }
    }

    private List<CoupangProductSearchResult> parseProducts(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        if (!root.path("rCode").asText("0").equals("0")) {
            throw new CoupangPartnersClientException("쿠팡 상품 검색 응답 오류: " + root.path("rMessage").asText());
        }

        List<CoupangProductSearchResult> products = new ArrayList<>();
        for (JsonNode product : root.path("data").path("productData")) {
            String productId = product.path("productId").asText();
            if (productId.isBlank()) {
                continue;
            }
            products.add(new CoupangProductSearchResult(
                    productId,
                    product.path("productName").asText(),
                    product.hasNonNull("productPrice") ? product.path("productPrice").decimalValue() : BigDecimal.ZERO,
                    product.path("productImage").asText(),
                    product.path("productUrl").asText(),
                    product.path("isRocket").asBoolean(),
                    product.path("isFreeShipping").asBoolean()
            ));
        }
        return products;
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

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
