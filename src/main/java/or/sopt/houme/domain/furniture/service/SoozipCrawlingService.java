package or.sopt.houme.domain.furniture.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    private static final String BASE_URL = "https://soozip.co.kr";
    private static final int DEFAULT_CATE_NO = 75;
    private static final long PAGE_DELAY_MILLIS = 250L;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String USER_AGENT = "Houme-SoozipCrawler/1.0";
    private static final String MALL_NAME = "SOOZIP";

    private static final Pattern PAGE_PARAM = Pattern.compile("[?&]page=(\\d+)");
    private static final Pattern PRODUCT_NO_PARAM = Pattern.compile("[?&]product_no=(\\d+)");
    private static final Pattern PRODUCT_PATH = Pattern.compile("/product/[^/]+/(\\d+)/");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public List<NaverFurnitureProductDto> fetchFurnitureProducts() {
        return fetchCategoryProducts(DEFAULT_CATE_NO, null);
    }

    public List<NaverFurnitureProductDto> fetchFurnitureProducts(Integer maxPages) {
        return fetchCategoryProducts(DEFAULT_CATE_NO, maxPages);
    }

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

    private Document fetchDocument(int cateNo, int page) {
        String url = BASE_URL + "/product/list.html?cate_no=" + cateNo + "&page=" + page;
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
            return Jsoup.parse(response.body(), BASE_URL);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to fetch Soozip list page: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to fetch Soozip list page: " + url, e);
        }
    }

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

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        if (url.startsWith("/")) {
            return BASE_URL + url;
        }
        if (!url.startsWith("http")) {
            return BASE_URL + "/" + url;
        }
        return url;
    }

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

    private Integer parseIntSafely(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLongSafely(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
