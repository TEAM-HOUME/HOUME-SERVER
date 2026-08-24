package or.sopt.houme.domain.coupang.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.domain.coupang.client.CoupangPartnersClient;
import or.sopt.houme.domain.coupang.client.CoupangPartnersClientException;
import or.sopt.houme.domain.coupang.service.CoupangBatchProperties;
import or.sopt.houme.domain.coupang.service.CoupangCollectionJobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * DB 영속 Job Queue에서 하나를 선점한 뒤, 전역 호출 간격을 지키며 쿠팡 상품을 수집합니다.
 * 외부 API 호출은 트랜잭션 밖에서 수행하고 결과 저장만 별도 짧은 트랜잭션으로 처리합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CoupangCollectionScheduler {

    private final CoupangCollectionJobService collectionJobService;
    private final CoupangPartnersClient coupangPartnersClient;
    private final CoupangBatchProperties batchProperties;

    @Scheduled(
            fixedDelayString = "${coupang.batch.poll-delay-ms:60000}",
            initialDelayString = "${coupang.batch.initial-delay-ms:60000}"
    )
    public void collectOneKeyword() {
        if (!batchProperties.isEnabled()) {
            return;
        }

        Optional<CoupangCollectionJobService.ClaimedCoupangJob> claimedJob = collectionJobService.claimNextJob();
        if (claimedJob.isEmpty()) {
            return;
        }

        CoupangCollectionJobService.ClaimedCoupangJob job = claimedJob.get();
        if (!collectionJobService.tryAcquireApiCallSlot()) {
            collectionJobService.releaseWithoutCalling(job.jobId());
            return;
        }

        try {
            List<CoupangProductSearchResult> products = coupangPartnersClient.searchProducts(
                    job.keyword(), batchProperties.getSearchLimit()
            );
            collectionJobService.completeJob(job.jobId(), products);
            log.info("쿠팡 상품 수집 완료: keyword={}, resultCount={}", job.keyword(), products.size());
        } catch (CoupangPartnersClientException e) {
            collectionJobService.failJob(job.jobId(), "COUPANG_API_ERROR", e.getMessage());
            log.warn("쿠팡 상품 수집 실패: keyword={}", job.keyword(), e);
        } catch (Exception e) {
            collectionJobService.failJob(job.jobId(), "COUPANG_BATCH_ERROR", e.getMessage());
            log.error("쿠팡 배치 처리 중 예기치 못한 오류: keyword={}", job.keyword(), e);
        }
    }
}
