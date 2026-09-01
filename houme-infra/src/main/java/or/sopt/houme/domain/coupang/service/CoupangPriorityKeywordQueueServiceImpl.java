package or.sopt.houme.domain.coupang.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.coupang.model.entity.CoupangCollectionJobJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordJpaEntity;
import or.sopt.houme.domain.coupang.repository.CoupangCollectionJobJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangKeywordJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoupangPriorityKeywordQueueServiceImpl implements CoupangPriorityKeywordQueueService {

    private final CoupangKeywordJpaRepository keywordRepository;
    private final CoupangCollectionJobJpaRepository jobRepository;

    /**
     * 단일 서버 전제에서 신규 키워드와 Job을 함께 생성한다.
     * 이미 등록된 키워드는 일반 순환을 방해하지 않도록 재정렬하지 않는다.
     */
    @Override
    @Transactional
    public synchronized void enqueueIfAbsent(String keyword, Long furnitureId) {
        String normalizedKeyword = keyword.trim();
        CoupangKeywordJpaEntity keywordEntity = keywordRepository.findByKeyword(normalizedKeyword)
                .orElseGet(() -> keywordRepository.save(CoupangKeywordJpaEntity.of(normalizedKeyword, furnitureId)));

        if (jobRepository.existsByKeyword(keywordEntity)) {
            return;
        }

        jobRepository.save(CoupangCollectionJobJpaEntity.priorityOf(keywordEntity, LocalDateTime.now()));
        log.info("사용자 요청 쿠팡 검색어를 우선 수집 큐에 등록했습니다. keyword={}", normalizedKeyword);
    }
}
