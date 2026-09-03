package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.MarketplaceSearchKeywords;
import or.sopt.houme.compare.domain.port.out.KeywordTranslationPort;
import or.sopt.houme.domain.coupang.service.CoupangPriorityKeywordQueueService;
import or.sopt.houme.furniture.domain.port.out.FurnitureRepositoryPort;
import org.springframework.stereotype.Service;

/**
 * 가격비교 요청 상품명에서 마켓별 검색어를 추출하고, 쿠팡 검색어를 수집 우선 큐에 반영합니다.
 * 실제 가격비교 API가 합쳐지면 스크래핑된 상품명만 이 유즈케이스에 전달합니다.
 */
@Service
@RequiredArgsConstructor
public class UserRequestedCoupangKeywordService {

    private final KeywordTranslationPort keywordTranslationPort;
    private final CoupangPriorityKeywordQueueService coupangPriorityKeywordQueueService;
    private final FurnitureRepositoryPort furnitureRepositoryPort;

    public MarketplaceSearchKeywords translateAndEnqueue(String productName) {
        MarketplaceSearchKeywords keywords = keywordTranslationPort.translateToMarketplaceKeywords(
                productName,
                furnitureRepositoryPort.findAllWithType()
        );
        coupangPriorityKeywordQueueService.enqueueIfAbsent(keywords.coupangKeyword(), keywords.furnitureId());
        return keywords;
    }
}
