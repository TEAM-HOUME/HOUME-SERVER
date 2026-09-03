package or.sopt.houme.compare.domain;

import java.util.List;
import java.util.stream.Collectors;

public record EbayProduct(
        Long id,
        String ebayItemId,
        String title,
        String imageUrl,
        double priceUsd,
        String productUrl,
        String soozipCategory,
        String titleEmbedding,   // pgvector format: "[0.1,0.2,...]"
        String imageEmbedding
) {
    public static EbayProduct forUpsert(
            String ebayItemId, String title, String imageUrl,
            double priceUsd, String productUrl, String soozipCategory,
            List<Double> titleEmbedding, List<Double> imageEmbedding
    ) {
        return new EbayProduct(
                null, ebayItemId, title, imageUrl, priceUsd, productUrl, soozipCategory,
                toVectorString(titleEmbedding), toVectorString(imageEmbedding)
        );
    }

    private static String toVectorString(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) return null;
        return "[" + embedding.stream().map(Object::toString).collect(Collectors.joining(",")) + "]";
    }
}
