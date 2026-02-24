package or.sopt.houme.domain.house.service.taste;

import or.sopt.houme.domain.house.presentation.taste.dto.response.MoodBoardListResponse;
import or.sopt.houme.domain.house.model.taste.entity.Taste;

import java.util.List;

public interface TasteService {

    // 무드 보드 이미지 제공 API
    MoodBoardListResponse getMoodboard(Long cursorId, int size);

    // 무드 보드 객체 제공
    List<Taste> getTasteList(List<Long> tasteList);
}
