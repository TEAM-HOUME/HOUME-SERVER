package or.sopt.houme.coupang.domain;

import java.math.BigDecimal;

/**
 * 쿠팡 파트너스 검색 결과를 애플리케이션 내부에서 사용하는 순수 값 객체입니다.
 */
public record CoupangProductSearchResult(
        String productId,
        String productName,
        BigDecimal productPrice,
        BigDecimal productDiscountRate,
        String productImage,
        String productUrl
) {
}
