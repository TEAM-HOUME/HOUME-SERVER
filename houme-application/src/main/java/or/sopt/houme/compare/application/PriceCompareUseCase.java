package or.sopt.houme.compare.application;

import or.sopt.houme.compare.domain.CompareJob;

public interface PriceCompareUseCase {

    CompareJob createJobByUrl(String sourceUrl);

    CompareJob getJob(String jobId);
}
