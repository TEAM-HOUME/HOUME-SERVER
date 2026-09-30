package or.sopt.houme.domain.furniture.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.application.AdminCurationEmbeddingUseCase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCurationEmbeddingService implements AdminCurationEmbeddingUseCase {

    private final CurationEmbeddingBatchService batchService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Async
    @Override
    public void triggerEmbeddingAsync(int limit) {
        if (!running.compareAndSet(false, true)) {
            log.warn("[임베딩 배치] 이미 실행 중 — 중복 요청 무시");
            return;
        }
        try {
            log.info("[임베딩 배치] 어드민 수동 시작: limit={}", limit);
            int processed = batchService.fillMissingEmbeddings(limit);
            log.info("[임베딩 배치] 어드민 수동 완료: processed={}", processed);
        } catch (Exception e) {
            log.error("[임베딩 배치] 어드민 수동 실패", e);
        } finally {
            running.set(false);
        }
    }
}
