package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.domain.CompareHistoryItem;
import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.OriginalProduct;
import or.sopt.houme.compare.domain.port.in.PriceCompareUseCase;
import or.sopt.houme.compare.domain.port.out.CompareJobStorePort;
import or.sopt.houme.compare.domain.port.out.GetCompareHistoryPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.ProductPageScrapePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCompareServiceImpl implements PriceCompareUseCase {

    private final CompareJobStorePort jobStore;
    private final EbayPipelineService pipelineService;
    private final ProductPageScrapePort productPageScrapePort;
    private final GetCompareHistoryPort getCompareHistoryPort;

    @Override
    public CompareJob createJobByUrl(String rawUrl) {
        SourceUrl sourceUrl = SourceUrl.normalize(rawUrl);

        // 동일 URL 진행 중인 job 재활용
        return jobStore.findActiveBySourceUrl(sourceUrl.value()).orElseGet(() -> {
            ScrapedProduct scraped = productPageScrapePort.scrape(sourceUrl);
            if (!scraped.hasEssentials()) {
                throw new PriceCompareException(ErrorCode.PRODUCT_METADATA_PARSE_FAILED);
            }

            OriginalProduct original = OriginalProduct.of(
                    scraped.title(),
                    scraped.thumbnailUrl(),
                    scraped.price() != null ? scraped.price().doubleValue() : null,
                    null
            );

            CompareJob job = new CompareJob(UUID.randomUUID().toString(), sourceUrl.value());
            job.setOriginalProduct(original);
            jobStore.save(job);
            pipelineService.runAsync(job);

            log.info("가격 비교 job 생성: jobId={}, url={}", job.getJobId(), sourceUrl.value());
            return job;
        });
    }

    @Override
    public CompareJob getJob(String jobId) {
        return jobStore.findById(jobId)
                .orElseThrow(() -> new PriceCompareException(ErrorCode.COMPARE_JOB_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompareHistoryItem> getHistory(Long userId, int limit) {
        return getCompareHistoryPort.findByUserId(userId, limit);
    }
}
