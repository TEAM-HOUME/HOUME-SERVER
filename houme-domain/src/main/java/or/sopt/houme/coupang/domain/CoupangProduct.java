package or.sopt.houme.coupang.domain;

public record CoupangProduct(
        Long id,
        String name,
        String imageUrl,
        String productUrl,
        Long currentPrice,
        Long estimatedOriginalPrice,
        Integer discountRate
) {}
