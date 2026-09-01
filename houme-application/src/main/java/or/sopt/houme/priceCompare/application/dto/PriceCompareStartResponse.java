package or.sopt.houme.priceCompare.application.dto;

import or.sopt.houme.priceCompare.domain.ScrapedProduct;

public record PriceCompareStartResponse(
        String jobId,
        String status,
        String sourceUrl,
        String title,
        String thumbnailUrl,
        String brand,
        Long price
) {

    public static PriceCompareStartResponse of(String jobId, ScrapedProduct product) {
        return new PriceCompareStartResponse(
                jobId,
                "PENDING",
                product.sourceUrl(),
                product.title(),
                product.thumbnailUrl(),
                product.brand(),
                product.price()
        );
    }
}
