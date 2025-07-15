package or.sopt.houme.domain.taste.service;

import java.util.List;

public interface TasteTagService {

    // 무드보드 중 우선순위 가장 높은 id만 반환
    Long getPriorityId(List<Long> ids);
}
