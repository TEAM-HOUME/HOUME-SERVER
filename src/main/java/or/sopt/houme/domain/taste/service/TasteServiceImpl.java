package or.sopt.houme.domain.taste.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.taste.dto.response.MoodBoardListResponse;
import or.sopt.houme.domain.taste.dto.response.MoodBoardResponse;
import or.sopt.houme.domain.taste.repository.taste.TasteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TasteServiceImpl implements TasteService {

    private final TasteRepository tasteRepository;

    // 무드보드 제공 (cursor 기반 페이지네이션)
    @Override
    public MoodBoardListResponse getMoodboard(Long cursorId, int size) {

        List<MoodBoardResponse> list = tasteRepository.findTasteByCursor(cursorId, size)
                .stream()
                .map(MoodBoardResponse::from)
                .toList();

        return MoodBoardListResponse.of(list);
    }
}
