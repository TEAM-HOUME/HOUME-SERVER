package or.sopt.houme.domain.taste.service;

import or.sopt.houme.domain.taste.entity.Tag;

import java.util.List;

public interface TasteTagService {

    // 무드보드 중 우선순위 가장 높은 Tag만 반환
    Tag getPriorityId(List<Long> tasteIds);
}
