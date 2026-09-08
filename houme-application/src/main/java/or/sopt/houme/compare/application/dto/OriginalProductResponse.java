package or.sopt.houme.compare.application.dto;

import or.sopt.houme.compare.domain.OriginalProduct;

public record OriginalProductResponse(
        String title,
        String imageUrl,
        Double price,
        String currency,
        String quality
) {
    public static OriginalProductResponse from(OriginalProduct p) {
        if (p == null) return null;
        return new OriginalProductResponse(p.title(), p.imageUrl(), p.price(), p.currency(), p.quality());
    }
}
