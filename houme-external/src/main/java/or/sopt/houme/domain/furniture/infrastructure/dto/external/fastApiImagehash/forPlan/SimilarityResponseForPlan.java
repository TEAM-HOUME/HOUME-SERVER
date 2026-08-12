package or.sopt.houme.domain.furniture.infrastructure.dto.external.fastApiImagehash.forPlan;

import java.util.List;

public record SimilarityResponseForPlan(
        List<RankedProduct> rankedProducts
) {
    public static SimilarityResponseForPlan of(List<RankedProduct> rankedProducts) {
        return new SimilarityResponseForPlan(rankedProducts);
    }

    public record RankedProduct(
            Long productId,
            String imageUrl,
            double similarity
    ) {
        public static RankedProduct of(Long productId, String imageUrl, double similarity) {
            return new RankedProduct(productId, imageUrl, similarity);
        }
    }
}
