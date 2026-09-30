package or.sopt.houme.domain.furniture.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.application.AdminCurationEmbeddingUseCase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCurationEmbeddingService implements AdminCurationEmbeddingUseCase {

    private final CurationEmbeddingBatchService batchService;

    @Override
    public boolean isRunning() {
        return batchService.isRunning();
    }

    @Async
    @Override
    public void triggerEmbeddingAsync(int limit) {
        try {
            log.info("[임베딩 배치] 어드민 수동 시작: limit={}", limit);
            int processed = batchService.fillMissingEmbeddings(limit);
            if (processed >= 0) {
                log.info("[임베딩 배치] 어드민 수동 완료: processed={}", processed);
            }
        } catch (Exception e) {
            log.error("[임베딩 배치] 어드민 수동 실패", e);
        }
    }
}
