package or.sopt.houme.compare.domain;

import java.util.List;

public record CurationCandidate(
        String source,
        String title,
        String imageUrl,
        Double price,
        String productUrl,
        List<Double> titleEmbedding,
        List<Double> imageEmbedding
) {}
