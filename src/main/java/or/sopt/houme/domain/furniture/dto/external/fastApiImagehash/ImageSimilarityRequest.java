package or.sopt.houme.domain.furniture.dto.external.fastApiImagehash;

import java.util.List;

public record ImageSimilarityRequest(
        String baseImageUrl,
        List<String> candidateImageUrls,
        int topK
) {
    public static ImageSimilarityRequest of(String baseImageUrl, List<String> candidateImageUrls, int topK) {
        return new ImageSimilarityRequest(baseImageUrl, candidateImageUrls, topK);
    }
}
