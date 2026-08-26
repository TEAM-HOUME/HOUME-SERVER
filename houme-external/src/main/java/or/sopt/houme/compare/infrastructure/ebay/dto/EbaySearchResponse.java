package or.sopt.houme.compare.infrastructure.ebay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EbaySearchResponse(
        int total,
        List<ItemSummary> itemSummaries
) {
    public record ItemSummary(
            String itemId,
            String title,
            Price price,
            List<ThumbnailImage> thumbnailImages,
            List<Category> categories,
            String itemWebUrl
    ) {}

    public record Price(String value, String currency) {}

    public record ThumbnailImage(String imageUrl) {}

    public record Category(String categoryId, String categoryName) {}
}
