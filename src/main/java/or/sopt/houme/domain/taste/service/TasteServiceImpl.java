package or.sopt.houme.domain.taste.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.taste.dto.response.MoodBoardListResponse;
import or.sopt.houme.domain.taste.dto.response.MoodBoardResponse;
import or.sopt.houme.domain.taste.repository.taste.TasteRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TasteServiceImpl implements TasteService {

    private final TasteRepository tasteRepository;

    // 무드보드 제공 (cursor 기반 페이지네이션)
    @Cacheable(value = "moodBoardListCache", key = "'cursor:' + #cursorId + ':size:' + #size")
    @Override
    public MoodBoardListResponse getMoodboard(Long cursorId, int size) {

        List<MoodBoardResponse> list = tasteRepository.findTasteByCursor(cursorId, size)
                .stream()
                .map(MoodBoardResponse::from)
                .collect(Collectors.toList());

        // 리스트 섞기
        Collections.shuffle(list);

        return MoodBoardListResponse.of(list);
    }
}
