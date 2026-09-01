package or.sopt.houme.compare.domain;

/** 하나의 상품명에서 추출한 마켓별 검색어입니다. */
public record MarketplaceSearchKeywords(
        String ebayKeyword,
        String coupangKeyword
) {
}
