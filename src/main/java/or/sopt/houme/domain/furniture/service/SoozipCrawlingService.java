package or.sopt.houme.domain.furniture.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.global.util.HtmlTextCleaner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SoozipCrawlingService {

    private static final int DEFAULT_CATE_NO = 75;
    private static final long PAGE_DELAY_MILLIS = 250L;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String USER_AGENT = "Houme-SoozipCrawler/1.0";
    private static final String MALL_NAME = "SOOZIP";

    private static final Pattern PAGE_PARAM = Pattern.compile("[?&]page=(\\d+)");
    private static final Pattern PRODUCT_NO_PARAM = Pattern.compile("[?&]product_no=(\\d+)");
    private static final Pattern PRODUCT_PATH = Pattern.compile("/product/[^/]+/(\\d+)/");

    @Value("${soozip.base-url}")
    private String baseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Soozip 기본 카테고리(cate_no=75) 상품을 모두 가져옵니다.
     * maxPages 제한이 없으면 마지막 페이지까지 순회합니다.
     */
    public List<NaverFurnitureProductDto> fetchFurnitureProducts() {
        return fetchCategoryProducts(DEFAULT_CATE_NO, null);
    }

    /**
     * 기본 카테고리 상품을 페이지 제한과 함께 가져옵니다.
     * maxPages가 비어있거나 유효하지 않으면 전체 페이지를 순회합니다.
     */
    public List<NaverFurnitureProductDto> fetchFurnitureProducts(Integer maxPages) {
        return fetchCategoryProducts(DEFAULT_CATE_NO, maxPages);
    }

    /**
     * 카테고리별 리스트 페이지를 순회하여 중복 없는 상품 목록을 반환합니다.
     * - 1페이지를 먼저 가져온 뒤 마지막 페이지를 파악합니다.
     * - productId 기준으로 페이지 간 중복을 제거합니다.
     * - 특정 페이지 결과가 비어있으면 조기 종료합니다.
     */
    public List<NaverFurnitureProductDto> fetchCategoryProducts(int cateNo, Integer maxPages) {
        Document first = fetchDocument(cateNo, 1);
        int lastPage = parseLastPage(first);
        if (maxPages != null && maxPages > 0) {
            lastPage = Math.min(lastPage, maxPages);
        }

        List<NaverFurnitureProductDto> products = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        addProducts(parseProducts(first), products, seen);

        for (int page = 2; page <= lastPage; page++) {
            sleep(PAGE_DELAY_MILLIS);
            Document doc = fetchDocument(cateNo, page);
            List<NaverFurnitureProductDto> pageProducts = parseProducts(doc);
            if (pageProducts.isEmpty()) {
                break;
            }
            addProducts(pageProducts, products, seen);
        }

        return products;
    }

    /**
     * 리스트 페이지 HTML을 내려받아 Jsoup으로 파싱합니다.
     * 네트워크 오류나 2xx가 아닌 응답은 예외로 처리합니다.
     */
    private Document fetchDocument(int cateNo, int page) {

        // 해당 주소로부터 cateNo을 기준으로 카테고리를 파싱합니다.
        String url = baseUrl() + "/product/list.html?cate_no=" + cateNo + "&page=" + page;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .header("User-Agent", USER_AGENT)
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Unexpected status: " + response.statusCode());
            }
            return Jsoup.parse(response.body(), baseUrl());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to fetch Soozip list page: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to fetch Soozip list page: " + url, e);
        }
    }

    /**
     * 페이지네이션에서 마지막 페이지 번호를 추출합니다.
     * ".last"가 없으면 모든 링크를 훑어 최댓값을 찾습니다.
     */
    private int parseLastPage(Document doc) {
        Element last = doc.selectFirst(".ec-base-paginate a.last");
        Integer page = parsePageFromHref(last == null ? null : last.attr("href"));
        if (page != null && page > 0) {
            return page;
        }

        int maxPage = 1;
        for (Element link : doc.select(".ec-base-paginate a[href*='page=']")) {
            Integer p = parsePageFromHref(link.attr("href"));
            if (p != null && p > maxPage) {
                maxPage = p;
            }
        }
        return maxPage;
    }

    /**
     * "?cate_no=75&page=3" 형태의 링크에서 page 파라미터를 추출합니다.
     */
    private Integer parsePageFromHref(String href) {
        if (href == null || href.isBlank()) {
            return null;
        }
        Matcher matcher = PAGE_PARAM.matcher(href);
        if (matcher.find()) {
            return parseIntSafely(matcher.group(1));
        }
        return null;
    }

    /**
     * Cafe24 리스트 DOM 구조를 기준으로 상품 DTO를 생성합니다.
     * - 목록: ul.prdList > li
     * - 이미지: .prdImg img (첫 번째 이미지)
     * - 링크: .prdImg a 또는 .description .name a
     * - 이름: .description .name a 내부 span 중 마지막 텍스트
     */
    private List<NaverFurnitureProductDto> parseProducts(Document doc) {
        List<NaverFurnitureProductDto> products = new ArrayList<>();
        Elements items = doc.select("ul.prdList > li");
        for (Element item : items) {
            String productUrl = extractProductUrl(item);
            String imageUrl = extractImageUrl(item);
            Long productId = extractProductId(item, productUrl);
            String name = extractProductName(item);

            if (name == null || name.isBlank()) {
                Element img = item.selectFirst(".prdImg img");
                if (img != null) {
                    name = img.attr("alt");
                }
            }

            if (productId == null || name == null || name.isBlank()
                    || productUrl == null || productUrl.isBlank()
                    || imageUrl == null || imageUrl.isBlank()) {
                continue;
            }

            products.add(new NaverFurnitureProductDto(
                    imageUrl,
                    productUrl,
                    HtmlTextCleaner.clean(name),
                    MALL_NAME,
                    productId
            ));
        }

        return products;
    }

    /**
     * 제목 앵커에서 상품명을 추출합니다.
     * 마지막 span 텍스트를 우선 사용하고, 없으면 앵커 텍스트를 사용합니다.
     */
    private String extractProductName(Element item) {
        Element nameAnchor = item.selectFirst(".description .name a");
        if (nameAnchor == null) {
            return null;
        }

        Elements spans = nameAnchor.select("span");
        for (int i = spans.size() - 1; i >= 0; i--) {
            String text = spans.get(i).text().trim();
            if (!text.isEmpty()) {
                return text;
            }
        }

        String text = nameAnchor.ownText().trim();
        if (text.isBlank()) {
            text = nameAnchor.text().trim();
        }

        return text.isBlank() ? null : text;
    }

    /**
     * 상품 상세 URL을 추출하고 상대경로를 정규화합니다.
     */
    private String extractProductUrl(Element item) {
        Element link = item.selectFirst(".prdImg a");
        if (link == null) {
            link = item.selectFirst(".description .name a");
        }
        if (link == null) {
            return null;
        }

        String url = link.absUrl("href");
        if (url == null || url.isBlank()) {
            url = link.attr("href");
        }

        return normalizeUrl(url);
    }

    /**
     * 상품 이미지 URL을 추출하고 프로토콜 없는 URL을 정규화합니다.
     */
    private String extractImageUrl(Element item) {
        Element img = item.selectFirst(".prdImg img");
        if (img == null) {
            return null;
        }

        String url = img.absUrl("src");
        if (url == null || url.isBlank()) {
            url = img.attr("src");
        }

        return normalizeUrl(url);
    }

    /**
     * URL을 정규화합니다.
     * - "//..." -> "https://..."
     * - "/..."  -> baseUrl + "/..."
     * - "path"  -> baseUrl + "/path"
     */
    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        if (url.startsWith("/")) {
            return baseUrl() + url;
        }
        if (!url.startsWith("http")) {
            return baseUrl() + "/" + url;
        }
        return url;
    }

    /**
     * productId를 우선순위대로 추출합니다.
     * 1) product_no 쿼리 파라미터
     * 2) "/product/.../{id}/" 경로 세그먼트
     * 3) "li#anchorBoxId_{id}" 폴백
     */
    private Long extractProductId(Element item, String productUrl) {
        Long productId = parseProductIdFromHref(productUrl);
        if (productId != null) {
            return productId;
        }

        Element link = item.selectFirst(".prdImg a, .description .name a");
        if (link != null) {
            productId = parseProductIdFromHref(link.attr("href"));
            if (productId != null) {
                return productId;
            }
        }

        String itemId = item.id();
        if (itemId != null && itemId.startsWith("anchorBoxId_")) {
            return parseLongSafely(itemId.substring("anchorBoxId_".length()));
        }

        return null;
    }

    /**
     * Cafe24 URL 패턴에서 productId를 파싱합니다.
     */
    private Long parseProductIdFromHref(String href) {
        if (href == null || href.isBlank()) {
            return null;
        }

        Matcher queryMatcher = PRODUCT_NO_PARAM.matcher(href);
        if (queryMatcher.find()) {
            return parseLongSafely(queryMatcher.group(1));
        }

        Matcher pathMatcher = PRODUCT_PATH.matcher(href);
        if (pathMatcher.find()) {
            return parseLongSafely(pathMatcher.group(1));
        }

        return null;
    }

    /**
     * 페이지 번호 파싱 유틸입니다.
     */
    private Integer parseIntSafely(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * productId 파싱 유틸입니다.
     */
    private Long parseLongSafely(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * productId 기준 중복 제거 후 목록에 추가합니다.
     */
    private void addProducts(List<NaverFurnitureProductDto> source,
                             List<NaverFurnitureProductDto> target,
                             Set<Long> seen) {
        for (NaverFurnitureProductDto product : source) {
            Long id = product.furnitureProductId();
            if (id == null || seen.contains(id)) {
                continue;
            }
            seen.add(id);
            target.add(product);
        }
    }

    private String baseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("SOOZIP_BASE_URL 환경변수가 필요합니다.");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /**
     * 페이지 요청 간 간단한 지연을 둡니다.
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
