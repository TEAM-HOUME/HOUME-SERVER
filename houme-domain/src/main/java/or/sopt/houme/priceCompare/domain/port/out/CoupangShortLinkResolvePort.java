package or.sopt.houme.priceCompare.domain.port.out;

import or.sopt.houme.priceCompare.domain.SourceUrl;

import java.util.Optional;

public interface CoupangShortLinkResolvePort {

    /**
     * 쿠팡 단축 링크를 리다이렉트로 풀어 정규화된 쿠팡 상품 URL 을 돌려준다.
     * 상품 URL 에 닿지 못하면 비어 있다.
     */
    Optional<SourceUrl> resolve(SourceUrl shortLink);
}
