package or.sopt.houme.compare.presentation.dto.response;

import java.time.OffsetDateTime;

public record PresetSimilarProductResponse(
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
