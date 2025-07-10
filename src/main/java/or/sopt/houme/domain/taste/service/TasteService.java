package or.sopt.houme.domain.taste.service;

import or.sopt.houme.domain.taste.dto.response.MoodBoardListResponse;

public interface TasteService {

    // 무드 보드 이미지 제공 API
    MoodBoardListResponse getMoodboard(Long cursorId, int size);
}
