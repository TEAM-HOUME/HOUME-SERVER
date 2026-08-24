package or.sopt.houme.domain.coupang.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.domain.coupang.model.entity.CoupangApiCallControlJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangCollectionJobJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangJobStatus;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordProductJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangProductPriceHistoryJpaEntity;
import or.sopt.houme.domain.coupang.repository.CoupangApiCallControlJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangCollectionJobJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangKeywordJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangKeywordProductJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangProductJpaRepository;
import or.sopt.houme.domain.coupang.repository.CoupangProductPriceHistoryJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoupangCollectionJobService {

    private static final Collection<CoupangJobStatus> CLAIMABLE_STATUSES = List.of(
            CoupangJobStatus.PENDING, CoupangJobStatus.RETRY_WAIT
    );

    private final CoupangKeywordJpaRepository keywordRepository;
    private final CoupangCollectionJobJpaRepository jobRepository;
    private final CoupangProductJpaRepository productRepository;
    private final CoupangKeywordProductJpaRepository keywordProductRepository;
    private final CoupangProductPriceHistoryJpaRepository priceHistoryRepository;
    private final CoupangApiCallControlJpaRepository apiCallControlRepository;
    private final CoupangBatchProperties batchProperties;

    @Transactional
    public void seedKeywords(List<CoupangSeedKeyword> seedKeywords) {
        LocalDateTime now = LocalDateTime.now();
        for (CoupangSeedKeyword seedKeyword : seedKeywords) {
            if (keywordRepository.existsByKeyword(seedKeyword.keyword())) {
                continue;
            }
            CoupangKeywordJpaEntity keyword = keywordRepository.save(
                    CoupangKeywordJpaEntity.of(seedKeyword.keyword(), seedKeyword.category())
            );
            jobRepository.save(CoupangCollectionJobJpaEntity.of(keyword, now));
        }
        if (!apiCallControlRepository.existsById(CoupangApiCallControlJpaEntity.SINGLETON_ID)) {
            apiCallControlRepository.save(CoupangApiCallControlJpaEntity.initial());
        }
    }

    @Transactional
    public Optional<ClaimedCoupangJob> claimNextJob() {
        LocalDateTime now = LocalDateTime.now();
        return jobRepository.findFirstByStatusInAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                        CLAIMABLE_STATUSES, now
                )
                .map(job -> {
                    job.claim(now);
                    return new ClaimedCoupangJob(job.getId(), job.getKeyword().getId(), job.getKeyword().getKeyword());
                });
    }

    @Transactional
    public boolean tryAcquireApiCallSlot() {
        LocalDateTime now = LocalDateTime.now();
        CoupangApiCallControlJpaEntity control = apiCallControlRepository
                .findWithLockById(CoupangApiCallControlJpaEntity.SINGLETON_ID)
                .orElseGet(() -> apiCallControlRepository.save(CoupangApiCallControlJpaEntity.initial()));
        if (!control.canAcquire(now, batchProperties.getMinimumCallIntervalMinutes())) {
            return false;
        }
        control.acquire(now);
        return true;
    }

    @Transactional
    public void releaseWithoutCalling(Long jobId) {
        jobRepository.findById(jobId).ifPresent(job -> job.requeue(LocalDateTime.now().plusMinutes(1)));
    }

    @Transactional
    public void completeJob(Long jobId, List<CoupangProductSearchResult> results) {
        CoupangCollectionJobJpaEntity job = jobRepository.findById(jobId).orElseThrow();
        CoupangKeywordJpaEntity keyword = job.getKeyword();
        keywordProductRepository.deleteByKeywordId(keyword.getId());

        for (CoupangProductSearchResult result : results) {
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
    public void failJob(Long jobId, String errorCode, String errorMessage) {
        jobRepository.findById(jobId).ifPresent(job -> {
            if (job.getRetryCount() < batchProperties.getMaxRetryCount()) {
                job.retry(LocalDateTime.now().plusMinutes(job.getRetryCount() + 1L), errorCode, abbreviate(errorMessage));
                return;
            }
            job.fail(LocalDateTime.now(), errorCode, abbreviate(errorMessage));
        });
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
