package or.sopt.houme.domain.coupang.service;

import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.domain.coupang.model.entity.CoupangCollectionJobJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordProductJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity;
import or.sopt.houme.domain.coupang.repository.CoupangCollectionJobJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangKeywordProductJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangProductJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangProductPriceHistoryJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoupangCollectionJobServiceTest {

    @Mock
    private CoupangCollectionJobJpaRepository jobRepository;
    @Mock
    private CoupangProductJpaRepository productRepository;
    @Mock
    private CoupangKeywordProductJpaRepository keywordProductRepository;
    @Mock
    private CoupangProductPriceHistoryJpaRepository priceHistoryRepository;
    @InjectMocks
    private CoupangCollectionJobService collectionJobService;

    @Test
    @DisplayName("기존 키워드 상품 매핑을 flush로 삭제한 뒤 중복 상품 ID는 한 번만 저장한다")
    void completesJobWithDistinctProductIdsAfterFlushingDelete() {
        CoupangKeywordJpaEntity keyword = CoupangKeywordJpaEntity.of("3인용 소파", 8L);
        CoupangCollectionJobJpaEntity job = CoupangCollectionJobJpaEntity.of(keyword, LocalDateTime.now());
        CoupangProductSearchResult duplicateProduct = new CoupangProductSearchResult(
                "1", "테스트 소파", new BigDecimal("100000"), BigDecimal.ZERO, "https://image", "https://product"
        );

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(productRepository.findByCoupangProductId("1")).thenReturn(Optional.empty());
        when(productRepository.save(any(CoupangProductJpaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        collectionJobService.completeJob(1L, List.of(duplicateProduct, duplicateProduct));

        InOrder inOrder = inOrder(keywordProductRepository);
        inOrder.verify(keywordProductRepository).deleteByKeywordId(keyword.getId());
        inOrder.verify(keywordProductRepository).flush();
        inOrder.verify(keywordProductRepository).save(any(CoupangKeywordProductJpaEntity.class));
        verify(keywordProductRepository, times(1)).save(any(CoupangKeywordProductJpaEntity.class));
        verify(productRepository, times(1)).save(any(CoupangProductJpaEntity.class));
    }
}
