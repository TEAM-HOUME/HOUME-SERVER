package or.sopt.houme.domain.furniture.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.client.FastApiImageHashClient;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.ImageHashRequest;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.SimilarityResponse;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.forPlan.ImageHashRequestForPlan;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.forPlan.SimilarityResponseForPlan;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.forPlan.FurnitureProductsInfoResponseForPlan;
import or.sopt.houme.domain.furniture.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.FastApiException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageHashService {

    private final FastApiImageHashClient fastApiImageHashClient;

    public List<FurnitureProductsInfoResponse.FurnitureProductInfo> rankByImageSimilarity(
            String baseImageUrl,
            List<NaverFurnitureProductDto> products,
            int topN
    ) {
        try {
            // FastAPI에 요청 보낼 DTO 구성
            ImageHashRequest request = ImageHashRequest.of(baseImageUrl, products);

            // 외부 API 호출
            SimilarityResponse response = fastApiImageHashClient.getTopKSimilarImages(request);

            if (response == null || response.rankedProducts() == null) {
                throw new FastApiException(ErrorCode.IMAGE_HASH_EMPTY_RESPONSE);
            }

            // productId로 빠르게 매핑
            Map<Long, NaverFurnitureProductDto> productMap = products.stream()
                    .collect(Collectors.toMap(NaverFurnitureProductDto::furnitureProductId, Function.identity()));

            // FastAPI 응답 기반으로 상위 N개 상품 매핑
            return response.rankedProducts().stream()
                    .limit(topN)
                    .map(r -> {
                        NaverFurnitureProductDto product = productMap.get(r.productId());

                        return FurnitureProductsInfoResponse.FurnitureProductInfo.of(
                                null, // ID는 추후 추가됩니다
                                product.furnitureProductImageUrl(),
                                product.furnitureProductSiteUrl(),
                                product.furnitureProductName(),
                                product.furnitureProductMallName(),
                                product.furnitureProductId(),
                                r.similarity()
                        );
                    })
                    .toList();
        } catch (FeignException e) {
            int status = e.status();

            // 4xx 에러 → 클라이언트 오류
            if (status >= 400 && status < 500) {
                throw new FastApiException(ErrorCode.IMAGE_HASH_CLIENT_ERROR);
            }

            // 5xx 에러 → 서버 오류
            if (status >= 500) {
                throw new FastApiException(ErrorCode.IMAGE_HASH_SERVER_ERROR);
            }

            // 그 외 예외
            throw new FastApiException(ErrorCode.IMAGE_HASH_SERVER_ERROR);

        } catch (Exception e) {
            throw new FastApiException(ErrorCode.IMAGE_HASH_SERVER_ERROR);
        }
    }


    // 기획의사결정용
    public List<FurnitureProductsInfoResponseForPlan.FurnitureProductInfo> rankByImageSimilarityForPlan(
            String baseImageUrl,
            List<NaverFurnitureProductDto> products,
            int pHash,
            int colorHash,
            int topN) {
        try {
            // FastAPI에 요청 보낼 DTO 구성
            ImageHashRequestForPlan request = ImageHashRequestForPlan.of(baseImageUrl, products, pHash, colorHash);

            // 외부 API 호출
            SimilarityResponseForPlan response = fastApiImageHashClient.getTopKSimilarImagesForPlan(request);

            if (response == null || response.rankedProducts() == null) {
                throw new FastApiException(ErrorCode.IMAGE_HASH_EMPTY_RESPONSE);
            }

            // productId로 빠르게 매핑
            Map<Long, NaverFurnitureProductDto> productMap = products.stream()
                    .collect(Collectors.toMap(NaverFurnitureProductDto::furnitureProductId, Function.identity()));

            // FastAPI 응답 기반으로 상위 N개 상품 매핑
            return response.rankedProducts().stream()
                    .limit(topN)
                    .map(r -> {
                        var product = productMap.get(r.productId());

                        return FurnitureProductsInfoResponseForPlan.FurnitureProductInfo.of(
                                baseImageUrl,
                                product.furnitureProductImageUrl(),
                                product.furnitureProductSiteUrl(),
                                product.furnitureProductName(),
                                product.furnitureProductMallName(),
                                String.valueOf(product.furnitureProductId()),
                                r.similarity()
                        );
                    })
                    .toList();
        } catch (FeignException e) {
            int status = e.status();

            // 4xx 에러 → 클라이언트 오류
            if (status >= 400 && status < 500) {
                throw new FastApiException(ErrorCode.IMAGE_HASH_CLIENT_ERROR);
            }

            // 5xx 에러 → 서버 오류
            if (status >= 500) {
                throw new FastApiException(ErrorCode.IMAGE_HASH_SERVER_ERROR);
            }

            // 그 외 예외
            throw new FastApiException(ErrorCode.IMAGE_HASH_SERVER_ERROR);

        } catch (Exception e) {
            throw new FastApiException(ErrorCode.IMAGE_HASH_SERVER_ERROR);
        }
    }
}
