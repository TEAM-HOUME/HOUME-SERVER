package or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.forPlan;

import java.util.List;

public record SimilarityResponseForPlan(
        List<or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.SimilarityResponse.RankedProduct> rankedProducts
) {
    public static or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.SimilarityResponse of(List<or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.SimilarityResponse.RankedProduct> rankedProducts) {
        return new or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.SimilarityResponse(rankedProducts);
    }

    public record RankedProduct(
            Long productId,
            String imageUrl,
            double similarity
    ) {
        public static or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.SimilarityResponse.RankedProduct of(Long productId, String imageUrl, double similarity) {
            return new or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.SimilarityResponse.RankedProduct(productId, imageUrl, similarity);
        }
    }
}
