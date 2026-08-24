package or.sopt.houme.coupang.presentation.dto;

import or.sopt.houme.coupang.domain.CoupangProductSearchResult;

import java.math.BigDecimal;

public record CoupangProductSearchResponse(
        String productId,
        String productName,
        BigDecimal price,
        String imageUrl,
        String productUrl,
        boolean rocket,
        boolean freeShipping
) {
    public static CoupangProductSearchResponse from(CoupangProductSearchResult result) {
        return new CoupangProductSearchResponse(
                result.productId(),
                result.productName(),
                result.productPrice(),
                result.productImage(),
                result.productUrl(),
                result.rocket(),
                result.freeShipping()
        );
    }
}
