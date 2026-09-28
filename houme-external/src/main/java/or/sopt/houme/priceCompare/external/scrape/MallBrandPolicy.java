package or.sopt.houme.priceCompare.external.scrape;

import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * 입점몰의 브랜드 칸을 몰 이름으로 채운다.
 *
 * <p>편집샵·입점몰은 {@code schema.org/Product} 의 {@code brand} 를 상품 브랜드로 쓰지 않는다.
 * 수집이 그렇다 — 같은 "리샘" 소파 3건인데 한 건만 {@code "SOOZIP 수집"} 이 들어오고 나머지는 비어,
 * 몰 안에서 값이 갈린다. 실제 브랜드("리샘")는 상품명 접두어로만 존재해 구조화 데이터로는 얻을 수 없다.
 *
 * <p>그래서 해당 몰에서는 브랜드 칸을 <b>몰 이름으로 통일</b>한다. 값을 얻지 못했다고 비워두는 것보다
 * 출처가 드러나고 몰 안에서 일관되기 때문이다. 상품의 제조 브랜드가 아니라는 점은 감수한다 —
 * 현재 {@code brand} 는 eBay·쿠팡 검색 입력으로 들어가지 않고
 * ({@code PriceCompareServiceImpl} 은 title·thumbnail·price 만 넘긴다) 응답 표시와 품질 판정에만 쓰인다.
 *
 * <p>{@code og:site_name} 을 그대로 쓰지 않는 이유는 몰이 거기에 프로모션 문구를 넣기 때문이다
 * (마켓비: {@code "모두 만원대 아이템! - 마켓비"}). 호스트별로 쓸 이름을 명시한다.
 *
 * <p>자사 브랜드를 파는 몰(이케아·마켓비)은 대상이 아니다 — 그쪽 {@code brand} 는 실제 브랜드라 정확하다.
 */
@Slf4j
@Component
public class MallBrandPolicy {

    /**
     * 브랜드 칸을 몰 이름으로 채울 호스트와 그때 쓸 이름.
     *
     * <p>자사 브랜드를 파는 몰을 여기 넣으면 실제 브랜드가 몰 이름으로 덮인다. 입점몰만 등록한다.
     */
    private static final Map<String, String> MALL_BRAND_NAMES = Map.of("soozip.co.kr", "수집");

    private static final List<String> SITE_NAME_SELECTORS = List.of(
            "meta[property=og:site_name]", "meta[name=application-name]");

    /**
     * 대상 몰이면 브랜드를 몰 이름으로 맞춘다.
     *
     * <p>비어 있으면 채우고, 몰이 제 이름을 넣어둔 경우({@code og:site_name} 과 같은 값)는 표기를 통일한다.
     * 둘 다 아니면 실제 브랜드가 들어온 것으로 보고 건드리지 않는다.
     */
    public ScrapedProduct apply(ScrapedProduct product, Document document, String sourceUrl) {
        String mallBrand = mallBrandOf(sourceUrl);
        if (mallBrand == null || mallBrand.equals(product.brand())) {
            return product;
        }

        String current = product.brand();
        boolean fillable = isBlank(current) || isSameName(current, siteNameOf(document));
        if (!fillable) {
            return product;
        }

        log.debug("입점몰 브랜드를 몰 이름으로 설정: url={}, before={}, after={}", sourceUrl, current, mallBrand);
        return new ScrapedProduct(
                product.sourceUrl(),
                product.title(),
                product.thumbnailUrl(),
                mallBrand,
                product.price(),
                product.currency(),
                product.additionalImageUrls(),
                product.description()
        );
    }

    private String mallBrandOf(String sourceUrl) {
        String host = hostOf(sourceUrl);
        if (host == null) {
            return null;
        }
        String normalized = host.toLowerCase();
        return MALL_BRAND_NAMES.entrySet().stream()
                .filter(each -> normalized.equals(each.getKey()) || normalized.endsWith("." + each.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String siteNameOf(Document document) {
        for (String selector : SITE_NAME_SELECTORS) {
            String content = document.select(selector).attr("content").trim();
            if (!content.isBlank()) {
                return content;
            }
        }
        return null;
    }

    private boolean isSameName(String brand, String siteName) {
        return siteName != null && normalize(brand).equals(normalize(siteName));
    }

    /** 공백·대소문자 차이로 판정이 갈리지 않도록 맞춘다. */
    private String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String hostOf(String sourceUrl) {
        try {
            return URI.create(sourceUrl).getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
