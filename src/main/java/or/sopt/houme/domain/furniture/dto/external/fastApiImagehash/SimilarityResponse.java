package or.sopt.houme.domain.furniture.dto.external.fastApiImagehash;

import java.util.List;

public record SimilarityResponse(
        List<RankedProduct> rankedProducts
) {
    public static SimilarityResponse of(List<RankedProduct> rankedProducts) {
        return new SimilarityResponse(rankedProducts);
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
