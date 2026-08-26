package or.sopt.houme.compare.domain;

import java.util.List;

public record SimilarProduct(
        String source,
        String title,
        String imageUrl,
        Double price,
        String currency,
        String productUrl,
        double similarityScore,
        List<EbayCategory> categories
) {
    public record EbayCategory(String categoryId, String categoryName) {}
}
