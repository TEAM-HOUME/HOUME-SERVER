package or.sopt.houme.compare.infrastructure.ebay;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.infrastructure.ebay.client.EbayBrowseClient;
import or.sopt.houme.compare.infrastructure.ebay.dto.EbayImageSearchRequest;
import or.sopt.houme.compare.infrastructure.ebay.dto.EbaySearchResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CompareException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EbaySearchAdapter {

    private final EbayBrowseClient ebayBrowseClient;
    private final EbayTokenManager tokenManager;

    public List<EbaySearchResponse.ItemSummary> search(String keyword, int limit) {
        try {
            String bearer = "Bearer " + tokenManager.getToken();
            EbaySearchResponse resp = ebayBrowseClient.search(
                    bearer,
                    "EBAY_US",
                    keyword,
                    limit,
                    "MATCHING_ITEMS"
            );
            if (resp == null || resp.itemSummaries() == null) {
                return Collections.emptyList();
            }
            return resp.itemSummaries();
        } catch (FeignException e) {
            log.error("eBay 검색 실패: keyword={}, status={}", keyword, e.status(), e);
            throw new CompareException(ErrorCode.COMPARE_EBAY_SEARCH_FAILED);
        }
    }

    public List<EbaySearchResponse.ItemSummary> searchByImage(String base64Image, int limit) {
        try {
            String bearer = "Bearer " + tokenManager.getToken();
            EbaySearchResponse resp = ebayBrowseClient.searchByImage(
                    bearer, "EBAY_US", limit, "MATCHING_ITEMS",
                    new EbayImageSearchRequest(base64Image)
            );
            if (resp == null || resp.itemSummaries() == null) {
                return Collections.emptyList();
            }
            return resp.itemSummaries();
        } catch (FeignException e) {
            log.error("eBay 이미지 검색 실패: status={}", e.status(), e);
            throw new CompareException(ErrorCode.COMPARE_EBAY_SEARCH_FAILED);
        }
    }
}
