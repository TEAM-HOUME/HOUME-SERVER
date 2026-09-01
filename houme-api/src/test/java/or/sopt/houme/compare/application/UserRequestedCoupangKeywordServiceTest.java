package or.sopt.houme.compare.application;

import or.sopt.houme.compare.domain.MarketplaceSearchKeywords;
import or.sopt.houme.compare.domain.port.out.KeywordTranslationPort;
import or.sopt.houme.domain.coupang.service.CoupangPriorityKeywordQueueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRequestedCoupangKeywordServiceTest {

    @Test
    @DisplayName("Gemini가 추출한 한국어 쿠팡 검색어를 우선 수집 큐에 등록한다")
    void enqueuesCoupangKeywordFromMarketplaceKeywords() {
        KeywordTranslationPort keywordTranslationPort = mock(KeywordTranslationPort.class);
        CoupangPriorityKeywordQueueService priorityKeywordQueueService = mock(CoupangPriorityKeywordQueueService.class);
        UserRequestedCoupangKeywordService service = new UserRequestedCoupangKeywordService(
                keywordTranslationPort,
                priorityKeywordQueueService
        );
        MarketplaceSearchKeywords keywords = new MarketplaceSearchKeywords("wood dining table", "원목 식탁");
        when(keywordTranslationPort.translateToMarketplaceKeywords("브랜드 원목 식탁 1200"))
                .thenReturn(keywords);

        MarketplaceSearchKeywords result = service.translateAndEnqueue("브랜드 원목 식탁 1200", 16L);

        verify(priorityKeywordQueueService).enqueueIfAbsent("원목 식탁", 16L);
        assertThat(result).isEqualTo(keywords);
    }
}
