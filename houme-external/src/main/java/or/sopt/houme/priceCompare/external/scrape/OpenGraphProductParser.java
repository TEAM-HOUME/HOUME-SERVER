package or.sopt.houme.priceCompare.external.scrape;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Open Graph / Twitter 카드 메타 태그 파서. 파서 체인의 2순위.
 *
 * <p>메신저에 링크를 붙였을 때 썸네일과 제목이 뜨게 하려고 거의 모든 몰이 넣어두는 태그다.
 * JSON-LD 만큼 필드가 많지는 않지만 채택률이 가장 높아, 1순위가 놓친 칸을 메우는 역할을 한다.
 */
@Component
@RequiredArgsConstructor
public class OpenGraphProductParser implements ProductPageParser {

    private static final int ORDER = 2;

    private final ProductImageUrlResolver imageUrlResolver;
    private final PriceTextParser priceTextParser;

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public Optional<ScrapedProduct> parse(Document document, String sourceUrl) {
        String title = firstContent(document, "meta[property=og:title]", "meta[name=twitter:title]");
        List<String> images = readImages(document, sourceUrl);
        String brand = firstContent(document, "meta[property=product:brand]", "meta[property=og:brand]");
        String description = firstContent(document,
                "meta[property=og:description]", "meta[name=twitter:description]");

        String priceText = firstContent(document,
                "meta[property=product:price:amount]", "meta[property=og:price:amount]");
        Long price = priceTextParser.parseAmount(priceText).orElse(null);
        String declaredCurrency = firstContent(document,
                "meta[property=product:price:currency]", "meta[property=og:price:currency]");
        String currency = price == null
                ? null
                : priceTextParser.resolveCurrency(declaredCurrency, priceText, hostOf(sourceUrl));

        ScrapedProduct parsed = new ScrapedProduct(
                sourceUrl,
                title,
                images.isEmpty() ? null : images.get(0),
                brand,
                price,
                currency,
                images.size() <= 1 ? List.of() : images.subList(1, images.size()),
                description
        );
        return hasAnyValue(parsed) ? Optional.of(parsed) : Optional.empty();
    }

    private List<String> readImages(Document document, String sourceUrl) {
        List<String> images = new ArrayList<>();
        List<String> selectors = List.of(
                "meta[property=og:image:secure_url]", "meta[property=og:image]", "meta[name=twitter:image]");

        for (String selector : selectors) {
            for (Element meta : document.select(selector)) {
                imageUrlResolver.resolve(meta.attr("content"), sourceUrl)
                        .filter(url -> !images.contains(url))
                        .ifPresent(images::add);
            }
        }
        return images;
    }

    private String firstContent(Document document, String... selectors) {
        for (String selector : selectors) {
            String content = document.select(selector).attr("content").trim();
            if (!content.isBlank()) {
                return content;
            }
        }
        return null;
    }

    private boolean hasAnyValue(ScrapedProduct product) {
        return product.title() != null
                || product.thumbnailUrl() != null
                || product.brand() != null
                || product.price() != null;
    }

    private String hostOf(String sourceUrl) {
        try {
            return URI.create(sourceUrl).getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
