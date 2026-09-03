package or.sopt.houme.compare.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record ComparePresetDetail(
        String sourceUrl,
        String title,
        String thumbnailUrl,
        String brand,
        Long price,
        String currency,
        List<SimilarItem> similarProducts
) {
    public record SimilarItem(
            String source,
            String productId,
            String title,
            String imageUrl,
            Double price,
            String currency,
            String siteName,
            String productUrl,
            OffsetDateTime priceUpdatedAt
    ) {}
}
