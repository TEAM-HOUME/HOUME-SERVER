package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.CompareHistoryItem;

import java.util.List;

public interface GetCompareHistoryPort {

    List<CompareHistoryItem> findByUserId(Long userId, int limit);
}
