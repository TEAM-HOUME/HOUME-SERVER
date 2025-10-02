package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.client.FastApiImageHashClient;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.ImageHashRequest;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.SimilarityResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.NaverFurnitureProductDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageHashService {

    private final FastApiImageHashClient fastApiImageHashClient;

    public List<FurnitureProductsInfoResponse.FurnitureProductInfo> rankByImageSimilarity(
            String baseImageUrl,
            List<NaverFurnitureProductDto> products,
            int topN
    ) {
        // FastAPI에 요청 보낼 DTO 구성
        ImageHashRequest request = ImageHashRequest.of(baseImageUrl, products);

        // 외부 API 호출
        SimilarityResponse response = fastApiImageHashClient.getTopKSimilarImages(request);

        // 응답 → 내부 Response DTO 매핑
        return response.rankedProducts().stream()
                .limit(topN)
                .map(r -> FurnitureProductsInfoResponse.FurnitureProductInfo.of(
                        r.imageUrl(),
                        r.siteUrl(),
                        r.name(),
                        r.mallName(),
                        r.similarity()
                ))
                .toList();
    }
}
