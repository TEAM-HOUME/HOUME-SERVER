package or.sopt.houme.compare.domain;

import java.util.List;
import java.util.stream.Collectors;

public record CompareCatalogItem(
        Long id,
        String ebayItemId,
        String title,
        String imageUrl,
        double priceUsd,
        String productUrl,
        String soozipCategory,
        String titleEmbedding  // pgvector format: "[0.1,0.2,...]"
) {
    public static CompareCatalogItem forUpsert(
            String ebayItemId, String title, String imageUrl,
            double priceUsd, String productUrl, String soozipCategory,
            List<Double> titleEmbedding
    ) {
        return new CompareCatalogItem(
                null, ebayItemId, title, imageUrl, priceUsd, productUrl, soozipCategory,
                toVectorString(titleEmbedding)
        );
    }

    private static String toVectorString(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) return null;
        return "[" + embedding.stream().map(Object::toString).collect(Collectors.joining(",")) + "]";
    }
}
