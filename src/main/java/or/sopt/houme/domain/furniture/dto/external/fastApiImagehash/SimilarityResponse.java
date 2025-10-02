package or.sopt.houme.domain.furniture.dto.external.fastApiImagehash;

import java.util.List;

public record SimilarityResponse(
        List<RankedProduct> rankedProducts
) {
    public static SimilarityResponse of(List<RankedProduct> rankedProducts) {
        return new SimilarityResponse(rankedProducts);
    }

    public record RankedProduct(
            String imageUrl,
            String siteUrl,
            String name,
            String mallName,
            double similarity
    ) {}
}
