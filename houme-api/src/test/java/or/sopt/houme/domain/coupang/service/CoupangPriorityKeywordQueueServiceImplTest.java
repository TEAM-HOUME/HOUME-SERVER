package or.sopt.houme.domain.coupang.service;

import or.sopt.houme.domain.coupang.model.entity.CoupangCollectionJobJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordJpaEntity;
import or.sopt.houme.domain.coupang.repository.CoupangCollectionJobJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangKeywordJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoupangPriorityKeywordQueueServiceImplTest {

    @Mock
    private CoupangKeywordJpaRepository keywordRepository;
    @Mock
    private CoupangCollectionJobJpaRepository jobRepository;
    @InjectMocks
    private CoupangPriorityKeywordQueueServiceImpl priorityKeywordQueueService;

    @Test
    @DisplayName("신규 사용자 요청 검색어는 우선 수집 Job으로 등록한다")
    void enqueuesNewKeywordAsPriorityJob() {
        when(keywordRepository.findByKeyword("원목 식탁")).thenReturn(Optional.empty());
        when(keywordRepository.save(any(CoupangKeywordJpaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.existsByKeyword(any(CoupangKeywordJpaEntity.class))).thenReturn(false);

        priorityKeywordQueueService.enqueueIfAbsent(" 원목 식탁 ", 16L);

        ArgumentCaptor<CoupangCollectionJobJpaEntity> jobCaptor = ArgumentCaptor.forClass(CoupangCollectionJobJpaEntity.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertThat(jobCaptor.getValue().isPriority()).isTrue();
        assertThat(jobCaptor.getValue().getKeyword().getKeyword()).isEqualTo("원목 식탁");
        assertThat(jobCaptor.getValue().getKeyword().getFurnitureId()).isEqualTo(16L);
    }

    @Test
    @DisplayName("이미 순환 큐에 있는 검색어는 재등록하거나 우선순위를 변경하지 않는다")
    void keepsExistingKeywordInCurrentQueueOrder() {
        CoupangKeywordJpaEntity existingKeyword = CoupangKeywordJpaEntity.of("원목 식탁", 16L);
        when(keywordRepository.findByKeyword("원목 식탁")).thenReturn(Optional.of(existingKeyword));
        when(jobRepository.existsByKeyword(existingKeyword)).thenReturn(true);

        priorityKeywordQueueService.enqueueIfAbsent("원목 식탁", 16L);

        verify(keywordRepository, never()).save(any());
        verify(jobRepository, never()).save(any());
    }

    @Test
    @DisplayName("키워드만 남고 Job이 없는 경우 우선 수집 Job으로 복구한다")
    void restoresMissingJobAsPriorityJob() {
        CoupangKeywordJpaEntity existingKeyword = CoupangKeywordJpaEntity.of("원목 식탁", 16L);
        when(keywordRepository.findByKeyword("원목 식탁")).thenReturn(Optional.of(existingKeyword));
        when(jobRepository.existsByKeyword(existingKeyword)).thenReturn(false);

        priorityKeywordQueueService.enqueueIfAbsent("원목 식탁", 16L);

        ArgumentCaptor<CoupangCollectionJobJpaEntity> jobCaptor = ArgumentCaptor.forClass(CoupangCollectionJobJpaEntity.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertThat(jobCaptor.getValue().isPriority()).isTrue();
        verify(keywordRepository, never()).save(any());
    }
}
