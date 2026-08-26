package or.sopt.houme.compare.presentation.dto.response;

import or.sopt.houme.compare.domain.SimilarProduct;

public record SimilarProductItemResponse(
        String source,
        String title,
        String imageUrl,
        Double price,
        String currency,
        String productUrl
) {
    public static SimilarProductItemResponse from(SimilarProduct p) {
        return new SimilarProductItemResponse(
                p.source(), p.title(), p.imageUrl(), p.price(),
                p.currency(), p.productUrl()
        );
    }
}
