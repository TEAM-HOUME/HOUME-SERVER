package or.sopt.houme.compare.infrastructure.ebay;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.domain.EbayCandidate;
import or.sopt.houme.compare.domain.port.out.EbaySearchPort;
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
public class EbaySearchAdapter implements EbaySearchPort {

    private final EbayBrowseClient ebayBrowseClient;
    private final EbayTokenManager tokenManager;

    @Override
    public List<EbayCandidate> search(String keyword, int limit) {
        try {
            String bearer = "Bearer " + tokenManager.getToken();
            EbaySearchResponse resp = ebayBrowseClient.search(bearer, "EBAY_US", keyword, limit, "MATCHING_ITEMS");
            if (resp == null || resp.itemSummaries() == null) return Collections.emptyList();
            return resp.itemSummaries().stream().map(this::toCandidate).toList();
        } catch (FeignException e) {
            log.error("eBay 검색 실패: keyword={}, status={}", keyword, e.status(), e);
            throw new CompareException(ErrorCode.COMPARE_EBAY_SEARCH_FAILED);
        }
    }

    @Override
    public List<EbayCandidate> searchByImage(String base64Image, int limit) {
        try {
            String bearer = "Bearer " + tokenManager.getToken();
            EbaySearchResponse resp = ebayBrowseClient.searchByImage(
                    bearer, "EBAY_US", limit, "MATCHING_ITEMS", new EbayImageSearchRequest(base64Image));
            if (resp == null || resp.itemSummaries() == null) return Collections.emptyList();
            return resp.itemSummaries().stream().map(this::toCandidate).toList();
        } catch (FeignException e) {
            log.error("eBay 이미지 검색 실패: status={}", e.status(), e);
            throw new CompareException(ErrorCode.COMPARE_EBAY_SEARCH_FAILED);
        }
    }

    private EbayCandidate toCandidate(EbaySearchResponse.ItemSummary item) {
        double price = 0.0;
        if (item.price() != null && item.price().value() != null) {
            try { price = Double.parseDouble(item.price().value()); } catch (NumberFormatException ignored) {}
        }
        String thumbnail = (item.thumbnailImages() != null && !item.thumbnailImages().isEmpty())
                ? item.thumbnailImages().get(0).imageUrl() : null;
        List<String> categoryIds = item.categories() == null ? List.of()
                : item.categories().stream().map(EbaySearchResponse.Category::categoryId).toList();
        return new EbayCandidate(item.itemId(), item.title(), price, thumbnail, item.itemWebUrl(), categoryIds);
    }
}
