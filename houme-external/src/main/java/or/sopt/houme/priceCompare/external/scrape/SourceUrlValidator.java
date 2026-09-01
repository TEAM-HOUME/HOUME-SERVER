package or.sopt.houme.priceCompare.external.scrape;

import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;

/**
 * 스크래핑 대상 주소가 외부로 나가도 되는 주소인지 검증한다. (SSRF 방어)
 *
 * <p>유저가 임의의 URL을 넣을 수 있는 구조이므로, 그대로 요청을 보내면
 * 서버가 사내망이나 EC2 메타데이터 엔드포인트(169.254.169.254)를 대신 긁어오는 통로가 된다.
 * 리다이렉트마다 다시 검증해야 DNS rebinding 도 막힌다 —
 * 그래서 {@link ProductPageFetcher} 는 자동 리다이렉트를 끄고 홉마다 이 검증을 다시 호출한다.
 */
@Slf4j
@Component
public class SourceUrlValidator {

    private static final List<String> ALLOWED_SCHEMES = List.of("http", "https");
    private static final List<Integer> ALLOWED_PORTS = List.of(-1, 80, 443);

    /**
     * 통과하면 정상, 위반이면 {@link ErrorCode#FORBIDDEN_PRODUCT_URL} 예외.
     */
    public void validate(URI uri) {
        String scheme = uri.getScheme() == null ? null : uri.getScheme().toLowerCase(Locale.ROOT);
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme)) {
            throw new PriceCompareException(ErrorCode.FORBIDDEN_PRODUCT_URL);
        }
        if (!ALLOWED_PORTS.contains(uri.getPort())) {
            throw new PriceCompareException(ErrorCode.FORBIDDEN_PRODUCT_URL);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new PriceCompareException(ErrorCode.FORBIDDEN_PRODUCT_URL);
        }

        for (InetAddress address : resolve(host)) {
            if (isInternal(address)) {
                log.warn("스크래핑 차단 - 내부망 주소 요청: host={}, resolved={}", host, address.getHostAddress());
                throw new PriceCompareException(ErrorCode.FORBIDDEN_PRODUCT_URL);
            }
        }
    }

    private InetAddress[] resolve(String host) {
        try {
            return InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new PriceCompareException(ErrorCode.INVALID_PRODUCT_URL);
        }
    }

    private boolean isInternal(InetAddress address) {
        return address.isAnyLocalAddress()      // 0.0.0.0
                || address.isLoopbackAddress()  // 127.0.0.0/8, ::1
                || address.isLinkLocalAddress() // 169.254.0.0/16 (EC2 메타데이터)
                || address.isSiteLocalAddress() // 10./172.16-31./192.168.
                || address.isMulticastAddress();
    }
}
