package or.sopt.houme.domain.furniture.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurationEmbeddingScheduler {

    private final CurationEmbeddingBatchService batchService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void fillEmbeddings() {
        log.info("[임베딩 배치] 시작");
        try {
            int processed = batchService.fillMissingEmbeddings();
            log.info("[임베딩 배치] 완료: processed={}", processed);
        } catch (Exception e) {
            log.error("[임베딩 배치] 실패", e);
        }
    }
}
