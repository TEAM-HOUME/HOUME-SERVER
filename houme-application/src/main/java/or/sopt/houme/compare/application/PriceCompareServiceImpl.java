package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.OriginalProduct;
import or.sopt.houme.compare.domain.port.in.PriceCompareUseCase;
import or.sopt.houme.compare.domain.port.out.CompareJobStorePort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CompareException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PriceCompareServiceImpl implements PriceCompareUseCase {

    private final CompareJobStorePort jobStore;
    private final EbayPipelineService pipelineService;

    @Override
    public CompareJob createJobByUrl(String sourceUrl) {
        // 동일 sourceUrl PENDING/RUNNING 중복 방지 (향후 scraping 구현 시 기존 job 재활용)
        return jobStore.findActiveBySourceUrl(sourceUrl).orElseThrow(
                () -> new CompareException(ErrorCode.COMPARE_SCRAPING_NOT_IMPLEMENTED)
        );
    }

    @Override
    public CompareJob createJobByDummy(OriginalProduct dummyProduct) {
        CompareJob job = new CompareJob(UUID.randomUUID().toString(), null);
        job.setOriginalProduct(dummyProduct);
        jobStore.save(job);
        pipelineService.runAsync(job);
        return job;
    }

    @Override
    public CompareJob getJob(String jobId) {
        return jobStore.findById(jobId)
                .orElseThrow(() -> new CompareException(ErrorCode.COMPARE_JOB_NOT_FOUND));
    }

}
