package or.sopt.houme.compare.infrastructure.ebay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.infrastructure.ebay.client.EbayOAuthClient;
import or.sopt.houme.compare.infrastructure.ebay.dto.EbayTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class EbayTokenManager {

    private static final String SCOPE = "https://api.ebay.com/oauth/api_scope";
    private static final int REFRESH_BUFFER_SECONDS = 30;

    private final EbayOAuthClient ebayOAuthClient;

    @Value("${ebay.client-id:}")
    private String clientId;

    @Value("${ebay.client-secret:}")
    private String clientSecret;

    private final AtomicReference<CachedToken> tokenRef = new AtomicReference<>();

    public String getToken() {
        CachedToken cached = tokenRef.get();
        if (cached != null && cached.isValid()) {
            return cached.accessToken;
        }
        return refresh();
    }

    private synchronized String refresh() {
        // double-check after acquiring lock
        CachedToken cached = tokenRef.get();
        if (cached != null && cached.isValid()) {
            return cached.accessToken;
        }
        String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        EbayTokenResponse resp = ebayOAuthClient.getToken(
                "Basic " + credentials,
                "client_credentials",
                SCOPE
        );
        Instant expiry = Instant.now().plusSeconds(resp.expiresIn() - REFRESH_BUFFER_SECONDS);
        tokenRef.set(new CachedToken(resp.accessToken(), expiry));
        log.debug("eBay 토큰 갱신 완료, 만료={}", expiry);
        return resp.accessToken();
    }

    private record CachedToken(String accessToken, Instant expiresAt) {
        boolean isValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
