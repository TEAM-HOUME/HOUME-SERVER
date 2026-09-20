package or.sopt.houme.priceCompare.external.scrape;

import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * 구조화 데이터가 선언한 브랜드를 믿어도 되는지 몰 단위로 판정한다.
 *
 * <p>편집샵·입점몰은 {@code schema.org/Product} 의 {@code brand} 에 입점 브랜드가 아니라
 * <b>몰 이름</b>을 그대로 넣어두는 경우가 있다. 수집이 그렇다 —
 * "리샘 모엘로 가죽소파"의 brand 가 {@code "SOOZIP 수집"} 으로 내려온다.
 * 그대로 두면 잘못된 값이 들어갈 뿐 아니라 brand 가 채워졌다는 이유로 품질이 FULL 로 승격되고,
 * 뒤이은 eBay·쿠팡 검색이 몰 이름을 브랜드로 알고 질의하게 된다.
 *
 * <p>"brand 가 {@code og:site_name} 과 같으면 버린다"는 전역 규칙은 쓸 수 없다 —
 * 이케아는 자사 브랜드를 파는 몰이라 {@code og:site_name} 과 {@code brand} 가 둘 다 {@code "IKEA"} 이고,
 * 이 경우의 brand 는 정답이다. 몰의 성격은 HTML 구조로 구분되지 않으므로 호스트로 분기한다.
 */
@Slf4j
@Component
public class MallBrandPolicy {

    /**
     * 구조화 데이터의 brand 가 상품 브랜드가 아니라 몰 이름인 호스트.
     *
     * <p>자사 브랜드를 파는 몰(이케아·마켓비)은 여기 넣으면 안 된다 — 그쪽 brand 는 정확하다.
     */
    private static final Set<String> MALL_NAME_AS_BRAND_HOSTS = Set.of("soozip.co.kr");

    private static final List<String> SITE_NAME_SELECTORS = List.of(
            "meta[property=og:site_name]", "meta[name=application-name]");

    /**
     * 몰 이름이 브랜드 자리에 들어온 경우 그 칸을 비운다.
     * 몰 전용 파서가 실제 브랜드를 찾아낸 경우(=몰 이름과 다른 값)는 건드리지 않는다.
     */
    public ScrapedProduct apply(ScrapedProduct product, Document document, String sourceUrl) {
        if (product.brand() == null || !isMallNameAsBrand(sourceUrl)) {
            return product;
        }

        String siteName = siteNameOf(document);
        if (!isSameName(product.brand(), siteName)) {
            return product;
        }

        log.debug("몰 이름이 브랜드로 선언되어 제외: url={}, brand={}", sourceUrl, product.brand());
        return new ScrapedProduct(
                product.sourceUrl(),
                product.title(),
                product.thumbnailUrl(),
                null,
                product.price(),
                product.currency(),
                product.additionalImageUrls(),
                product.description()
        );
    }

    private boolean isMallNameAsBrand(String sourceUrl) {
        String host = hostOf(sourceUrl);
        if (host == null) {
            return false;
        }
        String normalized = host.toLowerCase();
        return MALL_NAME_AS_BRAND_HOSTS.stream()
                .anyMatch(each -> normalized.equals(each) || normalized.endsWith("." + each));
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
        if (siteName == null) {
            return false;
        }
        return normalize(brand).equals(normalize(siteName));
    }

    /** 공백·대소문자 차이로 판정이 갈리지 않도록 맞춘다. */
    private String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String hostOf(String sourceUrl) {
        try {
            return URI.create(sourceUrl).getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
