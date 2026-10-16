package or.sopt.houme.priceCompare;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;
import or.sopt.houme.priceCompare.external.scrape.ProductPageFetcher;
import or.sopt.houme.priceCompare.external.scrape.SourceUrlValidator;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * 상품 페이지를 내려받으며 리다이렉트를 따라가는 동작 검증.
 *
 * <p>외부 사이트 상태에 좌우되지 않도록 로컬에 작은 HTTP 서버를 띄워, 실제 공유 링크가 거치는 이동을 흉내 낸다.
 * 로컬 주소는 SSRF 검증이 막으므로 검증기는 목으로 둔다 — 검증 자체는 {@code SourceUrlValidatorTest} 가 맡는다.
 */
@DisplayName("상품 페이지 내려받기")
class ProductPageFetcherTest {

    private static final String TRACKING_COOKIE = "abi_retry=true";

    private final ProductPageFetcher fetcher = new ProductPageFetcher(mock(SourceUrlValidator.class));
    private final List<String> cookiesAtTracker = new ArrayList<>();
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    @DisplayName("쿠키를 심고 자기 자신으로 되돌려 보내는 앱 공유 링크도 상품 페이지까지 따라간다")
    void 쿠키를_돌려줘야_넘어가는_공유_링크를_따라간다() {
        givenAppShareLink();

        Document document = fetcher.fetch(url("/share"));

        assertThat(document.title()).isEqualTo("머쉬룸 무드등");
    }

    @Test
    @DisplayName("쿠키는 요청 한 번 안에서만 쓰고 다음 요청에 들고 가지 않는다")
    void 쿠키를_다음_요청에_들고_가지_않는다() {
        givenAppShareLink();

        fetcher.fetch(url("/share"));
        fetcher.fetch(url("/share"));

        // 두 번째 요청도 쿠키 없이 시작해야 한다. 다른 사용자 요청에서 받은 쿠키가 섞이면 안 된다.
        assertThat(cookiesAtTracker).containsExactly("", TRACKING_COOKIE, "", TRACKING_COOKIE);
    }

    @Test
    @DisplayName("끝없이 되돌려 보내는 주소는 한도에서 멈추고 실패로 처리한다")
    void 끝없는_리다이렉트는_실패로_처리한다() {
        server.createContext("/loop", exchange -> redirect(exchange, "/loop"));

        assertThatThrownBy(() -> fetcher.fetch(url("/loop")))
                .isInstanceOf(PriceCompareException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_PAGE_FETCH_FAILED);
    }

    /**
     * 오늘의집 앱 공유 링크(ozip.me)가 실제로 거치는 이동을 본뜬다.
     * 공유 링크 → 추적 서버(쿠키를 심고 자기 자신으로) → 추적 서버(쿠키 확인 후 상품 주소로) → 상품 주소 → 상품 페이지.
     * 추적 서버는 쿠키를 돌려받기 전까지 같은 주소로 계속 되돌려 보낸다.
     */
    private void givenAppShareLink() {
        server.createContext("/share", exchange -> redirect(exchange, "/tracker"));
        server.createContext("/tracker", exchange -> {
            String cookie = cookieOf(exchange);
            cookiesAtTracker.add(cookie);
            if (cookie.contains(TRACKING_COOKIE)) {
                redirect(exchange, "/productions/1");
                return;
            }
            exchange.getResponseHeaders().add("Set-Cookie", TRACKING_COOKIE + "; Path=/; HttpOnly");
            redirect(exchange, "/tracker");
        });
        server.createContext("/productions/1", exchange -> redirect(exchange, "/goods/1"));
        server.createContext("/goods/1",
                exchange -> html(exchange, "<html><head><title>머쉬룸 무드등</title></head></html>"));
    }

    private String url(String path) {
        InetSocketAddress address = server.getAddress();
        return "http://" + address.getAddress().getHostAddress() + ":" + address.getPort() + path;
    }

    private String cookieOf(HttpExchange exchange) {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookie == null) {
            return "";
        }
        return cookie;
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private void html(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
