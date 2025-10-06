package or.sopt.houme.domain.taste.repository.taste_tag;

import or.sopt.houme.domain.taste.entity.Tag;

import java.util.List;
import java.util.Optional;

public interface TasteTagCustomRepository {

    // 우선순위 계산 후 반환
    Optional<Tag> findBestTasteId(List<Long> tasteIds);

    // 우선순위 계산 후 상위 2개 반환
    List<Tag> findBestTasteIdList(List<Long> tasteIds);

    // tasteIds에 해당하는 모든 TasteTag 조회
    List<Tag> findDistinctTagsByTasteIdIn(List<Long> tasteIds);
}
