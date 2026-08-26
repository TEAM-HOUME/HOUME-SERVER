package or.sopt.houme.domain.coupang.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.domain.coupang.config.CoupangPartnersProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 쿠팡 파트너스 검색 API의 HMAC 인증과 응답 변환을 담당합니다. */
@Component
@RequiredArgsConstructor
public class CoupangPartnersClient {

    private final CoupangPartnersProperties properties;
    private final ObjectMapper objectMapper;
    private final CoupangPartnersFeignClient coupangPartnersFeignClient;

    public List<CoupangProductSearchResult> searchProducts(String keyword, int limit) {
        if (!properties.isConfigured()) {
            throw new CoupangPartnersClientException("쿠팡 파트너스 API 키가 설정되지 않았습니다.");
        }

        try {
            return parseProducts(coupangPartnersFeignClient.searchProducts(keyword, limit));
        } catch (CoupangPartnersClientException e) {
            throw e;
        } catch (FeignException e) {
            throw new CoupangPartnersClientException("쿠팡 상품 검색 호출 실패: HTTP " + e.status(), e);
        } catch (Exception e) {
            throw new CoupangPartnersClientException("쿠팡 상품 검색 호출에 실패했습니다.", e);
        }
    }

    List<CoupangProductSearchResult> parseProducts(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode responseCode = root.get("rCode");
        if (responseCode == null || !"0".equals(responseCode.asText())) {
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
                    product.hasNonNull("productDiscountRate")
                            ? product.path("productDiscountRate").decimalValue()
                            : BigDecimal.ZERO,
                    product.path("productImage").asText(),
                    product.path("productUrl").asText()
            ));
        }
        return products;
    }

}
