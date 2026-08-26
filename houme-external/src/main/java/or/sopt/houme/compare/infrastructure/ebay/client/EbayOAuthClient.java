package or.sopt.houme.compare.infrastructure.ebay.client;

import or.sopt.houme.compare.infrastructure.ebay.dto.EbayTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "ebayOAuthClient",
        url = "${ebay.oauth-base-url:https://api.ebay.com}"
)
public interface EbayOAuthClient {

    @PostMapping(
            value = "/identity/v1/oauth2/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    EbayTokenResponse getToken(
            @RequestHeader("Authorization") String basicAuth,
            @RequestParam("grant_type") String grantType,
            @RequestParam("scope") String scope
    );
}
