package or.sopt.houme.domain.furniture.client;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.dto.NaverFurnitureProductDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NaverShopApiClient {

    private final RestTemplate restTemplate;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    private static final String BASE_URL = "https://openapi.naver.com/v1/search/shop.json";

    public List<NaverFurnitureProductDto> searchProducts(String query, int display) {
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("query", query) // 검색어
                .queryParam("display", display) // 검색 개수
                .queryParam("exclude", "used:rental:cbshop") // 중고, 렌탈 등등 상품은 제외
                .queryParam("filter", "naverpay") // 네이버페이 인증된 상품만 검색하는 것이 중복이 적음
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // REST 요청
        ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
        List<NaverFurnitureProductDto> results = new ArrayList<>();

        for (Map<String, Object> item : items) {
            results.add(NaverFurnitureProductDto.from(item));
        }

        return results;
    }
}
