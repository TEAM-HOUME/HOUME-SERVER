package or.sopt.houme.priceCompare.external.scrape;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JSON-LD(schema.org Product) 파서. 파서 체인의 1순위.
 *
 * <p>대부분의 쇼핑몰은 구글 검색 결과에 가격·별점을 노출하려고
 * {@code <script type="application/ld+json">} 안에 상품 정보를 구조화해 넣어둔다.
 * 화면 마크업과 달리 규격이 정해져 있어 사이트 개편에 잘 깨지지 않고,
 * OG 태그보다 담긴 필드도 많아 가장 먼저 시도한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonLdProductParser implements ProductPageParser {

    private static final String PRODUCT_TYPE = "Product";
    private static final int ORDER = 1;

    private final ObjectMapper objectMapper;
    private final ProductImageUrlResolver imageUrlResolver;
    private final PriceTextParser priceTextParser;

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public Optional<ScrapedProduct> parse(Document document, String sourceUrl) {
        for (Element script : document.select("script[type=application/ld+json]")) {
            Optional<ScrapedProduct> parsed = readProductNode(script.data())
                    .map(product -> toScrapedProduct(product, sourceUrl))
                    .filter(this::hasAnyValue);
            if (parsed.isPresent()) {
                return parsed;
            }
        }
        return Optional.empty();
    }

    private boolean hasAnyValue(ScrapedProduct product) {
        return product.title() != null
                || product.thumbnailUrl() != null
                || product.brand() != null
                || product.price() != null;
    }

    private Optional<JsonNode> readProductNode(String json) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return findProduct(objectMapper.readTree(json));
        } catch (Exception e) {
            // 몰 쪽 JSON 이 깨져 있는 경우가 드물지 않다. 다음 스크립트/파서로 넘어가면 되므로 조용히 스킵한다.
            log.debug("JSON-LD 파싱 스킵: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** 최상위가 배열이거나 {@code @graph} 로 감싸인 경우가 흔해 재귀로 훑는다. */
    private Optional<JsonNode> findProduct(JsonNode node) {
        if (node == null || node.isNull()) {
            return Optional.empty();
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                Optional<JsonNode> found = findProduct(child);
                if (found.isPresent()) {
                    return found;
                }
            }
            return Optional.empty();
        }
        if (!node.isObject()) {
            return Optional.empty();
        }
        if (hasProductType(node.get("@type"))) {
            return Optional.of(node);
        }
        return findProduct(node.get("@graph"));
    }

    private boolean hasProductType(JsonNode typeNode) {
        if (typeNode == null) {
            return false;
        }
        if (typeNode.isArray()) {
            for (JsonNode type : typeNode) {
                if (PRODUCT_TYPE.equalsIgnoreCase(type.asText())) {
                    return true;
                }
            }
            return false;
        }
        return PRODUCT_TYPE.equalsIgnoreCase(typeNode.asText());
    }

    private ScrapedProduct toScrapedProduct(JsonNode product, String sourceUrl) {
        List<String> images = readImages(product.get("image"), sourceUrl);
        JsonNode offer = firstOffer(product.get("offers"));

        String priceText = priceTextOf(offer);
        Long price = priceTextParser.parseAmount(priceText).orElse(null);
        String currency = price == null
                ? null
                : priceTextParser.resolveCurrency(currencyOf(offer), priceText, hostOf(sourceUrl));

        return new ScrapedProduct(
                sourceUrl,
                text(product.get("name")),
                images.isEmpty() ? null : images.get(0),
                readBrand(product.get("brand")),
                price,
                currency,
                images.size() <= 1 ? List.of() : images.subList(1, images.size()),
                text(product.get("description"))
        );
    }

    /** {@code image} 는 문자열·문자열 배열·{@code {"url": ...}} 객체 어느 형태로도 온다. */
    private List<String> readImages(JsonNode imageNode, String sourceUrl) {
        List<String> images = new ArrayList<>();
        collectImages(imageNode, sourceUrl, images);
        return images;
    }

    private void collectImages(JsonNode node, String sourceUrl, List<String> collected) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collectImages(child, sourceUrl, collected));
            return;
        }
        String raw = node.isObject() ? text(node.get("url")) : node.asText();
        imageUrlResolver.resolve(raw, sourceUrl)
                .filter(url -> !collected.contains(url))
                .ifPresent(collected::add);
    }

    /** {@code brand} 도 문자열이거나 {@code {"name": ...}} 객체다. */
    private String readBrand(JsonNode brandNode) {
        if (brandNode == null || brandNode.isNull()) {
            return null;
        }
        return brandNode.isObject() ? text(brandNode.get("name")) : text(brandNode);
    }

    /**
     * 가격을 읽을 offer 를 고른다. 배열이면 첫 번째 것을 쓴다.
     *
     * <p>할인 중인 상품(이케아 등)은 {@code AggregateOffer} 로 내려와 자기 {@code price} 가 없고,
     * 판매가는 안쪽 {@code offers} 에만 있다. 그런 경우 안쪽 offer 를 고른다.
     */
    private JsonNode firstOffer(JsonNode offersNode) {
        if (offersNode == null || offersNode.isNull()) {
            return null;
        }
        if (offersNode.isArray()) {
            if (offersNode.isEmpty()) {
                return null;
            }
            return firstOffer(offersNode.get(0));
        }
        if (text(offersNode.get("price")) != null) {
            return offersNode;
        }
        JsonNode inner = firstOffer(offersNode.get("offers"));
        if (inner != null && text(inner.get("price")) != null) {
            return inner;
        }
        return offersNode;
    }

    /**
     * offer 의 판매가 문자열. 안쪽 offer 없이 가격 범위만 둔 {@code AggregateOffer} 는
     * {@code price} 대신 {@code lowPrice}(최저가) 에 판매가를 둔다.
     * 할인 중인 이케아 상품처럼 가격이 {@code priceSpecification} 에만 있으면 거기서 판매가를 읽는다.
     */
    private String priceTextOf(JsonNode offer) {
        if (offer == null) {
            return null;
        }
        String price = text(offer.get("price"));
        if (price != null) {
            return price;
        }
        String lowPrice = text(offer.get("lowPrice"));
        if (lowPrice != null) {
            return lowPrice;
        }
        JsonNode spec = sellingPriceSpecification(offer);
        if (spec == null) {
            return null;
        }
        return text(spec.get("price"));
    }

    private String currencyOf(JsonNode offer) {
        if (offer == null) {
            return null;
        }
        String currency = text(offer.get("priceCurrency"));
        if (currency != null) {
            return currency;
        }
        JsonNode spec = sellingPriceSpecification(offer);
        if (spec == null) {
            return null;
        }
        return text(spec.get("priceCurrency"));
    }

    /**
     * {@code priceSpecification} 에서 판매가 항목을 고른다.
     * 판매가와 함께 취소선 정가({@code priceType=StrikethroughPrice})가 들어오므로 정가는 건너뛴다.
     */
    private JsonNode sellingPriceSpecification(JsonNode offer) {
        JsonNode specsNode = offer.get("priceSpecification");
        if (specsNode == null || specsNode.isNull()) {
            return null;
        }
        List<JsonNode> specs = new ArrayList<>();
        if (specsNode.isArray()) {
            specsNode.forEach(specs::add);
        } else {
            specs.add(specsNode);
        }
        for (JsonNode spec : specs) {
            if (!isStrikethroughPrice(spec) && text(spec.get("price")) != null) {
                return spec;
            }
        }
        return null;
    }

    private boolean isStrikethroughPrice(JsonNode spec) {
        String priceType = text(spec.get("priceType"));
        return priceType != null && priceType.endsWith("StrikethroughPrice");
    }

    private String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String hostOf(String sourceUrl) {
        try {
            return URI.create(sourceUrl).getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
