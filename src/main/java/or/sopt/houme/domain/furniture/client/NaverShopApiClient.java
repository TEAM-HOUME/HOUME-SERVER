package or.sopt.houme.domain.furniture.client;

import or.sopt.houme.domain.furniture.dto.external.naverShop.NaverShopResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "naverShopClient",
        url = "https://openapi.naver.com/v1/search"
)
public interface NaverShopApiClient {

    @GetMapping("/shop.json")
    NaverShopResponse searchProducts(
            @RequestHeader("X-Naver-Client-Id") String clientId,
            @RequestHeader("X-Naver-Client-Secret") String clientSecret,
            @RequestParam("query") String query,
            @RequestParam("display") int display,
            @RequestParam("exclude") String exclude,
            @RequestParam("filter") String filter
    );
}
