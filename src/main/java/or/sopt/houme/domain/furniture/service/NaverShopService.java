package or.sopt.houme.domain.furniture.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.client.NaverShopApiClient;
import or.sopt.houme.domain.furniture.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.dto.external.naverShop.NaverShopResponse;
import or.sopt.houme.global.api.handler.NaverApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import or.sopt.houme.global.api.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NaverShopService {

    private final NaverShopApiClient naverShopApiClient;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    /**
     * 네이버 쇼핑 API를 통해 특정 키워드로 상품 검색
     *
     * @param keyword 검색어
     * @param display 검색 결과 개수
     * @return 변환된 상품 DTO 리스트
     */
    public List<NaverFurnitureProductDto> search(String keyword, int display) {
        try {
            NaverShopResponse response = naverShopApiClient.searchProducts(
                    clientId, clientSecret, keyword, display,
                    "used:rental:cbshop", "naverpay"
            );

            // 응답이 비었을때 에러 핸들링
            if (response == null || response.items() == null) {
                throw new NaverApiException(ErrorCode.NAVER_API_EMPTY_RESPONSE);
            }

            return response.items().stream()
                    .map(NaverFurnitureProductDto::from)
                    .toList();

        } catch (FeignException e) {
            int status = e.status();

            // 4xx 에러 → 클라이언트 오류
            if (status >= 400 && status < 500) {
                throw new NaverApiException(ErrorCode.NAVER_API_CLIENT_ERROR);
            }

            // 5xx 에러 → 서버 오류
            if (status >= 500) {
                throw new NaverApiException(ErrorCode.NAVER_API_SERVER_ERROR);
            }

            // 그 외
            throw new NaverApiException(ErrorCode.NAVER_API_SERVER_ERROR);

        } catch (Exception e) {
            throw new NaverApiException(ErrorCode.NAVER_API_SERVER_ERROR);
        }
    }
}
