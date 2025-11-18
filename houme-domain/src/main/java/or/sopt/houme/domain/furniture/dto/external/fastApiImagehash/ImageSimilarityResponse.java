package or.sopt.houme.domain.furniture.dto.external.fastApiImagehash;

import java.util.List;

public record ImageSimilarityResponse(
        List<Result> results
) {
    public static ImageSimilarityResponse of(List<Result> results) {
        return new ImageSimilarityResponse(results);
    }

    public record Result(
            String imageUrl,
            double similarity,
            double shapeSimilarity,
            double colorSimilarity
    ) {
        public static Result of(String imageUrl, double similarity, double shapeSimilarity, double colorSimilarity) {
            return new Result(imageUrl, similarity, shapeSimilarity, colorSimilarity);
        }
    }
}
