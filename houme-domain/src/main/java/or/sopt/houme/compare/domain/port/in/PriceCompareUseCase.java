package or.sopt.houme.compare.domain.port.in;

import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.OriginalProduct;

public interface PriceCompareUseCase {

    /**
     * URL 모드: sourceUrl로 job 생성 (PENDING/RUNNING 중복 시 기존 반환)
     */
    CompareJob createJobByUrl(String sourceUrl);

    /**
     * 더미 모드: dummyProduct로 job 생성 (항상 새 job)
     */
    CompareJob createJobByDummy(OriginalProduct dummyProduct);

    CompareJob getJob(String jobId);
}
