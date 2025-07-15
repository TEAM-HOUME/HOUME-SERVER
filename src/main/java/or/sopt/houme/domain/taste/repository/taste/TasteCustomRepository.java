package or.sopt.houme.domain.taste.repository.taste;

import or.sopt.houme.domain.taste.entity.Taste;

import java.util.List;

public interface TasteCustomRepository {

    // cursor 기반 페이지네이션
    List<Taste> findTasteByCursor(Long cursorId, int size);

}
