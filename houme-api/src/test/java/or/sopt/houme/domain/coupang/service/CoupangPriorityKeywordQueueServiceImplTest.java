package or.sopt.houme.domain.coupang.service;

import or.sopt.houme.domain.coupang.model.entity.CoupangCollectionJobJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordJpaEntity;
import or.sopt.houme.domain.coupang.repository.CoupangCollectionJobJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangKeywordJpaRepository;
import or.sopt.houme.furniture.domain.Furniture;
import or.sopt.houme.furniture.domain.port.out.FurnitureRepositoryPort;
import or.sopt.houme.global.api.handler.CoupangException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoupangPriorityKeywordQueueServiceImplTest {

    @Mock
    private CoupangKeywordJpaRepository keywordRepository;
    @Mock
    private CoupangCollectionJobJpaRepository jobRepository;
    @Mock
    private FurnitureRepositoryPort furnitureRepositoryPort;
    @InjectMocks
    private CoupangPriorityKeywordQueueServiceImpl priorityKeywordQueueService;

    @Test
    @DisplayName("신규 사용자 요청 검색어는 우선 수집 Job으로 등록한다")
    void enqueuesNewKeywordAsPriorityJob() {
        givenExistingFurniture(16L);
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
        givenExistingFurniture(16L);
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
        givenExistingFurniture(16L);
        CoupangKeywordJpaEntity existingKeyword = CoupangKeywordJpaEntity.of("원목 식탁", 16L);
        when(keywordRepository.findByKeyword("원목 식탁")).thenReturn(Optional.of(existingKeyword));
        when(jobRepository.existsByKeyword(existingKeyword)).thenReturn(false);

        priorityKeywordQueueService.enqueueIfAbsent("원목 식탁", 16L);

        ArgumentCaptor<CoupangCollectionJobJpaEntity> jobCaptor = ArgumentCaptor.forClass(CoupangCollectionJobJpaEntity.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertThat(jobCaptor.getValue().isPriority()).isTrue();
        verify(keywordRepository, never()).save(any());
    }

    @Test
    @DisplayName("null, 빈 값, 100자 초과 키워드는 저장 전에 거절한다")
    void rejectsInvalidKeywordBeforePersistence() {
        assertThatThrownBy(() -> priorityKeywordQueueService.enqueueIfAbsent(null, 16L))
                .isInstanceOf(CoupangException.class);
        assertThatThrownBy(() -> priorityKeywordQueueService.enqueueIfAbsent("   ", 16L))
                .isInstanceOf(CoupangException.class);
        assertThatThrownBy(() -> priorityKeywordQueueService.enqueueIfAbsent("가".repeat(101), 16L))
                .isInstanceOf(CoupangException.class);

        verify(keywordRepository, never()).findByKeyword(any());
        verify(jobRepository, never()).save(any());
    }

    @Test
    @DisplayName("null 또는 존재하지 않는 가구 ID는 저장 전에 거절한다")
    void rejectsInvalidFurnitureIdBeforePersistence() {
        assertThatThrownBy(() -> priorityKeywordQueueService.enqueueIfAbsent("원목 식탁", null))
                .isInstanceOf(CoupangException.class);
        when(furnitureRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> priorityKeywordQueueService.enqueueIfAbsent("원목 식탁", 999L))
                .isInstanceOf(CoupangException.class);

        verify(keywordRepository, never()).findByKeyword(any());
        verify(jobRepository, never()).save(any());
    }

    private void givenExistingFurniture(Long furnitureId) {
        when(furnitureRepositoryPort.findById(furnitureId)).thenReturn(Optional.of(mock(Furniture.class)));
    }
}
