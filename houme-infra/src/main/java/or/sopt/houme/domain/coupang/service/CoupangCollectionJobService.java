package or.sopt.houme.domain.coupang.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.domain.coupang.model.entity.CoupangCollectionJobJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangJobStatus;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordProductJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangProductPriceHistoryJpaEntity;
import or.sopt.houme.domain.coupang.repository.CoupangCollectionJobJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangKeywordProductJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangProductJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangProductPriceHistoryJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoupangCollectionJobService {

    private static final int RUNNING_JOB_TIMEOUT_HOURS = 1;

    private final CoupangCollectionJobJpaRepository jobRepository;
    private final CoupangProductJpaRepository productRepository;
    private final CoupangKeywordProductJpaRepository keywordProductRepository;
    private final CoupangProductPriceHistoryJpaRepository priceHistoryRepository;

    @Transactional
    public Optional<ClaimedCoupangJob> claimNextJob() {
        LocalDateTime now = LocalDateTime.now();
        return jobRepository.findFirstByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                        CoupangJobStatus.PENDING, now
                )
                .map(job -> {
                    job.claim(now);
                    return new ClaimedCoupangJob(job.getId(), job.getKeyword().getId(), job.getKeyword().getKeyword());
                });
    }

    @Transactional
    public void completeJob(Long jobId, List<CoupangProductSearchResult> results) {
        CoupangCollectionJobJpaEntity job = jobRepository.findById(jobId).orElseThrow();
        CoupangKeywordJpaEntity keyword = job.getKeyword();
        keywordProductRepository.deleteByKeywordId(keyword.getId());
        keywordProductRepository.flush();

        Map<String, CoupangProductSearchResult> distinctResults = new LinkedHashMap<>();
        for (CoupangProductSearchResult result : results) {
            distinctResults.putIfAbsent(result.productId(), result);
        }

        for (CoupangProductSearchResult result : distinctResults.values()) {
            CoupangProductJpaEntity product = productRepository.findByCoupangProductId(result.productId())
                    .map(existing -> updateProduct(existing, result))
                    .orElseGet(() -> createProduct(result));
            keywordProductRepository.save(CoupangKeywordProductJpaEntity.of(keyword, product));
        }

        LocalDateTime now = LocalDateTime.now();
        keyword.markSucceeded(now);
        job.returnToQueueTail(now);
    }

    @Transactional
    public void failAndReturnToQueueTail(Long jobId, String errorCode, String errorMessage) {
        jobRepository.findById(jobId).ifPresent(job ->
                job.failAndReturnToQueueTail(LocalDateTime.now(), errorCode, abbreviate(errorMessage))
        );
    }

    @Transactional
    public int recoverExpiredRunningJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<CoupangCollectionJobJpaEntity> expiredJobs = jobRepository.findAllByStatusAndStartedAtBefore(
                CoupangJobStatus.RUNNING,
                now.minusHours(RUNNING_JOB_TIMEOUT_HOURS)
        );
        expiredJobs.forEach(job -> job.recoverFromRunningTimeout(now));
        return expiredJobs.size();
    }

    private CoupangProductJpaEntity createProduct(CoupangProductSearchResult result) {
        CoupangProductJpaEntity product = productRepository.save(CoupangProductJpaEntity.from(result));
        priceHistoryRepository.save(CoupangProductPriceHistoryJpaEntity.of(product, result.productPrice()));
        return product;
    }

    private CoupangProductJpaEntity updateProduct(CoupangProductJpaEntity product, CoupangProductSearchResult result) {
        BigDecimal previousPrice = product.getCurrentPrice();
        product.apply(result);
        if (previousPrice.compareTo(result.productPrice()) != 0) {
            priceHistoryRepository.save(CoupangProductPriceHistoryJpaEntity.of(product, result.productPrice()));
        }
        return product;
    }

    private String abbreviate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }

    public record ClaimedCoupangJob(Long jobId, Long keywordId, String keyword) {
    }
}
