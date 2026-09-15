package or.sopt.houme.domain.coupang.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.coupang.service.CoupangBatchProperties;
import or.sopt.houme.domain.coupang.service.CoupangProductImageEmbeddingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoupangProductImageEmbeddingScheduler {

    private final CoupangProductImageEmbeddingService productImageEmbeddingService;
    private final CoupangBatchProperties batchProperties;

    @Scheduled(cron = "${coupang.batch.embedding-cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void retryMissingImageEmbeddings() {
        if (!batchProperties.isCoupangCollectionBatchEnabled()) {
            return;
        }

        int targetCount = productImageEmbeddingService.embedMissingImages(batchProperties.getEmbeddingBatchSize());
        if (targetCount > 0) {
            log.info("쿠팡 상품 이미지 임베딩 재시도 완료: targetCount={}", targetCount);
        }
    }
}
