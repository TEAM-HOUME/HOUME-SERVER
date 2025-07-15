package or.sopt.houme.domain.taste.repository.taste_tag;

import java.util.List;

public interface TasteTagCustomRepository {

    // 우선순위 계산 후 반환
    Long findBestTasteId(List<Long> ids);
}
