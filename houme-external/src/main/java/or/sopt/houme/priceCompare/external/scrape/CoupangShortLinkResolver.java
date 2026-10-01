package or.sopt.houme.priceCompare.external.scrape;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.handler.PriceCompareException;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.CoupangShortLinkResolvePort;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

/**
 * 쿠팡 단축 링크를 리다이렉트 목적지로 풀어 상품 URL 을 얻는다.
 *
 * <p>상품 페이지(`www.coupang.com`)가 서버 요청을 403 으로 막는 동안에도
 * 단축 링크 서버(`link.coupang.com`)는 302 로 목적지를 알려줬다(실측).
 * 그래서 목적지에서 상품 ID 가 보이는 순간 멈추고, 막히는 상품 페이지는 여기서 요청하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoupangShortLinkResolver implements CoupangShortLinkResolvePort {

    private static final int MAX_HOPS = 3;

    private final ProductPageFetcher productPageFetcher;

    @Override
    public Optional<SourceUrl> resolve(SourceUrl shortLink) {
        URI current = URI.create(shortLink.value());
        for (int hop = 0; hop < MAX_HOPS; hop++) {
            Optional<URI> next = locationOf(current);
            if (next.isEmpty()) {
                log.warn("쿠팡 단축 링크 해석 실패 - 리다이렉트 없음: url={}, at={}", shortLink.value(), current);
                return Optional.empty();
            }
            Optional<SourceUrl> product = asCoupangProduct(next.get());
            if (product.isPresent()) {
                log.info("쿠팡 단축 링크 해석: url={}, product={}", shortLink.value(), product.get().value());
                return product;
            }
            current = next.get();
        }
        log.warn("쿠팡 단축 링크 해석 실패 - 리다이렉트 한도 초과: url={}", shortLink.value());
        return Optional.empty();
    }

    private Optional<URI> locationOf(URI target) {
        try {
            return productPageFetcher.redirectLocation(target);
        } catch (PriceCompareException e) {
            return Optional.empty();
        }
    }

    private Optional<SourceUrl> asCoupangProduct(URI location) {
        try {
            SourceUrl candidate = SourceUrl.normalize(location.toString());
            return candidate.coupangProductId().isPresent() ? Optional.of(candidate) : Optional.empty();
        } catch (PriceCompareException e) {
            return Optional.empty();
        }
    }
}
