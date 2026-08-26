package or.sopt.houme.compare.infrastructure.ebay.client;

import or.sopt.houme.compare.infrastructure.ebay.dto.EbayImageSearchRequest;
import or.sopt.houme.compare.infrastructure.ebay.dto.EbaySearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "ebayBrowseClient",
        url = "${ebay.api-base-url:https://api.ebay.com}"
)
public interface EbayBrowseClient {

    @GetMapping("/buy/browse/v1/item_summary/search")
    EbaySearchResponse search(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("X-EBAY-C-MARKETPLACE-ID") String marketplaceId,
            @RequestParam("q") String keyword,
            @RequestParam("limit") int limit,
            @RequestParam("fieldgroups") String fieldgroups
    );

    @PostMapping(value = "/buy/browse/v1/item_summary/search_by_image",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    EbaySearchResponse searchByImage(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("X-EBAY-C-MARKETPLACE-ID") String marketplaceId,
            @RequestParam("limit") int limit,
            @RequestParam("fieldgroups") String fieldgroups,
            @RequestBody EbayImageSearchRequest request
    );
}
