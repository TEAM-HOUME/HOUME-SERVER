package or.sopt.houme.compare.application;

import or.sopt.houme.compare.domain.MarketplaceSearchKeywords;
import or.sopt.houme.compare.domain.port.out.KeywordTranslationPort;
import or.sopt.houme.domain.coupang.service.CoupangPriorityKeywordQueueService;
import or.sopt.houme.furniture.domain.FurnitureWithTypeView;
import or.sopt.houme.furniture.domain.port.out.FurnitureRepositoryPort;

import java.util.List;
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
        FurnitureRepositoryPort furnitureRepositoryPort = mock(FurnitureRepositoryPort.class);
        UserRequestedCoupangKeywordService service = new UserRequestedCoupangKeywordService(
                keywordTranslationPort,
                priorityKeywordQueueService,
                furnitureRepositoryPort
        );
        List<FurnitureWithTypeView> furnitureCandidates = List.of(
                new FurnitureWithTypeView(16L, "dining table", "식탁", 1, 3L, "테이블", "table")
        );
        MarketplaceSearchKeywords keywords = new MarketplaceSearchKeywords("wood dining table", "원목 식탁", 16L);
        when(furnitureRepositoryPort.findAllWithType()).thenReturn(furnitureCandidates);
        when(keywordTranslationPort.translateToMarketplaceKeywords("브랜드 원목 식탁 1200", furnitureCandidates))
                .thenReturn(keywords);

        MarketplaceSearchKeywords result = service.translateAndEnqueue("브랜드 원목 식탁 1200");

        verify(priorityKeywordQueueService).enqueueIfAbsent("원목 식탁", 16L);
        assertThat(result).isEqualTo(keywords);
    }
}
