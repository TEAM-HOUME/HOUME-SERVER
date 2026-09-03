package or.sopt.houme.compare.domain.port.in;

import or.sopt.houme.compare.domain.CompareHistoryItem;
import or.sopt.houme.compare.domain.CompareJob;

import java.util.List;

public interface PriceCompareUseCase {

    CompareJob createJobByUrl(String sourceUrl);

    CompareJob getJob(String jobId);

    List<CompareHistoryItem> getHistory(Long userId, int limit);
}
