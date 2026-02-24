package or.sopt.houme.domain.furniture.service;

import feign.FeignException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.infrastructure.client.NaverShopApiClient;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverShopResponse;
import or.sopt.houme.global.api.handler.NaverApiException;
import or.sopt.houme.global.config.NaverProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import or.sopt.houme.global.api.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NaverShopService {

    private final NaverShopApiClient naverShopApiClient;
    private final NaverProperties naverProperties;

    /**
     * 네이버 쇼핑 API를 통해 특정 키워드로 상품 검색
     *
     * @param keyword 검색어
     * @param display 검색 결과 개수
     * @return 변환된 상품 DTO 리스트
     */
    public List<NaverFurnitureProductDto> search(String keyword, int display) {

        String clientId = naverProperties.getClientId();
        String clientSecret = naverProperties.getClientSecret();
        List<String> allowedMalls = naverProperties.getAllowedMalls();

        try {
            NaverShopResponse response = naverShopApiClient.searchProducts(
                    clientId, clientSecret, keyword, display,
                    "used:rental:cbshop", ""  // (임시) 네이버페이 필터링 옵션 제거
            );

            // 응답이 비었을때 에러 핸들링
            if (response == null || response.items() == null) {
                throw new NaverApiException(ErrorCode.NAVER_API_EMPTY_RESPONSE);
            }

            // mallName이 "롯데ON"인 상품만 필터링
            List<NaverFurnitureProductDto> responseLists = response.items().stream()
                    // (임시) mallName 필터링 제거
//                    .filter(item -> {
//                        Object mallName = item.get("mallName");
//                        return mallName != null &&
//                                allowedMalls.contains(mallName.toString().trim());
//                    })
                    .map(NaverFurnitureProductDto::from)
                    .toList();



            return responseLists;

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

    /**
     * 네이버 쇼핑 API를 통해 특정 키워드로 상품 검색 (필터 파라미터 버전)
     *
     * @param keyword     검색어
     * @param display     검색 결과 개수
     * @param allowedMalls mallName 허용 목록 (null 또는 비어있으면 필터 미적용)
     * @param payFilter   네이버페이 필터 문자열 (null 또는 빈 문자열이면 미적용)
     * @return 변환된 상품 DTO 리스트
     */
    public List<NaverFurnitureProductDto> searchV2(
            String keyword,
            int display,
            List<String> allowedMalls,
            String payFilter
    ) {

        String clientId = naverProperties.getClientId();
        String clientSecret = naverProperties.getClientSecret();

        try {
            NaverShopResponse response = naverShopApiClient.searchProducts(
                    clientId, clientSecret, keyword, display,
                    "used:rental:cbshop", payFilter == null ? "" : payFilter
            );

            if (response == null || response.items() == null) {
                throw new NaverApiException(ErrorCode.NAVER_API_EMPTY_RESPONSE);
            }

            return response.items().stream()
                    .filter(item -> {
                        if (allowedMalls == null || allowedMalls.isEmpty()) return true;
                        Object mallName = item.get("mallName");
                        return mallName != null && allowedMalls.contains(mallName.toString().trim());
                    })
                    .map(NaverFurnitureProductDto::from)
                    .toList();

        } catch (FeignException e) {
            int status = e.status();

            if (status >= 400 && status < 500) {
                throw new NaverApiException(ErrorCode.NAVER_API_CLIENT_ERROR);
            }

            if (status >= 500) {
                throw new NaverApiException(ErrorCode.NAVER_API_SERVER_ERROR);
            }

            throw new NaverApiException(ErrorCode.NAVER_API_SERVER_ERROR);

        } catch (Exception e) {
            throw new NaverApiException(ErrorCode.NAVER_API_SERVER_ERROR);
        }
    }
}
