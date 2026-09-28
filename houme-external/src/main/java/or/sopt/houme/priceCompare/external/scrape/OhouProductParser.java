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
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 오늘의집 전용 파서. 파서 체인의 3순위(몰 전용).
 *
 * <p>오늘의집은 JSON-LD 를 두지 않고 OG 태그에도 브랜드를 내려주지 않아
 * 1·2순위만으로는 {@code brand} 가 항상 비는데, 정작 브랜드는 Next.js 가 심어둔
 * {@code __NEXT_DATA__} 안에 들어 있다. 여기서 그 칸만 메운다.
 *
 * <p>주의할 점은 한 문서에 {@code brandName} 이 스무 번 넘게 나온다는 것이다 —
 * 대부분 광고 캐러셀·추천 상품의 브랜드라, 먼저 만나는 값을 집으면 남의 상품 브랜드가 들어간다.
 * 그래서 URL 의 상품 ID 와 {@code id} 가 일치하는 노드만 채택한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OhouProductParser implements ProductPageParser {

    private static final int ORDER = 3;

    private static final String NEXT_DATA_SELECTOR = "script#__NEXT_DATA__";
    private static final String HOST_SUFFIX = "ohou.se";
    private static final Pattern GOODS_ID = Pattern.compile("/goods/(\\d+)");

    private static final String ID_FIELD = "id";
    private static final String BRAND_FIELD = "brand";
    private static final String BRAND_NAME_FIELD = "brandName";
    private static final String NAME_FIELD = "name";

    /** 상품 노드를 찾다 남의 페이지 구조를 만났을 때 무한정 파고들지 않도록 둔 상한. */
    private static final int MAX_DEPTH = 20;

    private final ObjectMapper objectMapper;

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public Optional<ScrapedProduct> parse(Document document, String sourceUrl) {
        if (!isOhou(sourceUrl)) {
            return Optional.empty();
        }

        Optional<Long> goodsId = parseGoodsId(sourceUrl);
        if (goodsId.isEmpty()) {
            return Optional.empty();
        }

        Element nextData = document.selectFirst(NEXT_DATA_SELECTOR);
        if (nextData == null) {
            return Optional.empty();
        }

        return readBrand(nextData.data(), goodsId.get())
                .map(brand -> new ScrapedProduct(
                        sourceUrl, null, null, brand, null, null, List.of(), null));
    }

    private boolean isOhou(String sourceUrl) {
        try {
            String host = URI.create(sourceUrl).getHost();
            return host != null
                    && (host.equalsIgnoreCase(HOST_SUFFIX) || host.toLowerCase().endsWith("." + HOST_SUFFIX));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Optional<Long> parseGoodsId(String sourceUrl) {
        Matcher matcher = GOODS_ID.matcher(sourceUrl);
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(matcher.group(1)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * {@code __NEXT_DATA__} 를 훑어 요청한 상품 ID 와 일치하는 노드의 브랜드명을 찾는다.
     * 깨진 JSON 은 파서 하나의 실패로 끝내고 체인 전체를 막지 않는다.
     */
    private Optional<String> readBrand(String json, long goodsId) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return findBrand(objectMapper.readTree(json), goodsId, 0);
        } catch (Exception e) {
            log.warn("오늘의집 __NEXT_DATA__ 파싱 실패: goodsId={}, message={}", goodsId, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> findBrand(JsonNode node, long goodsId, int depth) {
        if (node == null || depth > MAX_DEPTH) {
            return Optional.empty();
        }

        if (node.isObject() && matchesGoods(node, goodsId)) {
            Optional<String> brand = brandOf(node);
            if (brand.isPresent()) {
                return brand;
            }
        }

        for (JsonNode child : node) {
            Optional<String> brand = findBrand(child, goodsId, depth + 1);
            if (brand.isPresent()) {
                return brand;
            }
        }
        return Optional.empty();
    }

    /** 상품 노드인지 판정한다. ID 만으로는 리뷰·주문 등 다른 엔티티와 겹칠 수 있어 이름도 함께 본다. */
    private boolean matchesGoods(JsonNode node, long goodsId) {
        JsonNode id = node.get(ID_FIELD);
        return id != null
                && id.isNumber()
                && id.asLong() == goodsId
                && isPresent(node.path(NAME_FIELD));
    }

    private Optional<String> brandOf(JsonNode node) {
        JsonNode nested = node.path(BRAND_FIELD).path(NAME_FIELD);
        if (isPresent(nested)) {
            return Optional.of(nested.asText().trim());
        }
        JsonNode flat = node.path(BRAND_NAME_FIELD);
        return isPresent(flat) ? Optional.of(flat.asText().trim()) : Optional.empty();
    }

    private boolean isPresent(JsonNode node) {
        return node != null && node.isTextual() && !node.asText().isBlank();
    }
}
