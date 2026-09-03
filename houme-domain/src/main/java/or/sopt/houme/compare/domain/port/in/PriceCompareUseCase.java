package or.sopt.houme.compare.domain.port.in;

import or.sopt.houme.compare.domain.CompareJob;

public interface PriceCompareUseCase {

    CompareJob createJobByUrl(String sourceUrl);

    CompareJob getJob(String jobId);
}
