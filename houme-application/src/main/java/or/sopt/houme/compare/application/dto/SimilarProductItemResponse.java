package or.sopt.houme.compare.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import or.sopt.houme.compare.domain.SimilarProduct;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SimilarProductItemResponse(
        String source,
        String productId,
        String title,
        String imageUrl,
        Double price,
        String currency,
        String productUrl
) {
    public static SimilarProductItemResponse from(SimilarProduct p) {
        return new SimilarProductItemResponse(
                p.source(), p.productId(), p.title(), p.imageUrl(), p.price(),
                p.currency(), p.productUrl()
        );
    }
}
