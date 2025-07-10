package or.sopt.houme.domain.taste.service;

import or.sopt.houme.domain.taste.dto.response.MoodBoardListResponse;
import or.sopt.houme.domain.taste.dto.response.MoodBoardResponse;
import or.sopt.houme.domain.taste.entity.Taste;
import or.sopt.houme.domain.taste.repository.TasteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[Taste Service] Test")
class TasteServiceImplTest {

    @Autowired
    TasteRepository tasteRepository;

    @Autowired
    TasteService tasteService;

    @Test
    @DisplayName("cursor 기반으로 페이지네이션을 구현 할 수 있다.")
    void getMoodBoardWithCursor() {
        // Given
        String url = "http://localhost:8080";
        String fileName = "fileName";
        String originalFilename = "originalFilename";
        String fileExtension = "jpg";
        String prompt = "prompt";

        List<Taste> tastes = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            tastes.add(Taste.builder()
                    .url(url + i)
                    .filename(fileName + i)
                    .originalFilename(originalFilename + i)
                    .fileExtension(fileExtension)
                    .tastePrompt(prompt + i)
                    .build());
        }

        tasteRepository.saveAll(tastes);

        Long cursorId = 15L;
        int limit = 10;

        // When
        MoodBoardListResponse moodboard = tasteService.getMoodboard(cursorId, limit);

        // Then
        assertThat(moodboard.moodBoardResponseList()).hasSize(limit);

        // 무드보드 리스트의 첫 번째 요소의 ID가 cursorId보다 작아야 함
        Long firstId = moodboard.moodBoardResponseList().get(0).id();
        assertThat(firstId).isLessThan(cursorId);

        // 모든 ID가 내림차순으로 정렬되어야 함
        var ids = moodboard.moodBoardResponseList().stream()
                .map(MoodBoardResponse::id)
                .toList();
        for (int i = 0; i < ids.size() - 1; i++) {
            assertThat(ids.get(i)).isGreaterThan(ids.get(i + 1));
        }

    }

    @Test
    @DisplayName("cursor가 null이면 최신순을 기반으로 페이지네이션을 구현 할 수 있다.")
    void getMoodBoardWithCursorNull() {
        // Given
        String url = "http://localhost:8080";
        String fileName = "fileName";
        String originalFilename = "originalFilename";
        String fileExtension = "jpg";
        String prompt = "prompt";

        List<Taste> tastes = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            tastes.add(Taste.builder()
                    .url(url + i)
                    .filename(fileName + i)
                    .originalFilename(originalFilename + i)
                    .fileExtension(fileExtension)
                    .tastePrompt(prompt + i)
                    .build());
        }

        tasteRepository.saveAll(tastes);

        Long cursorId = null;
        int limit = 10;

        // When
        MoodBoardListResponse moodboard = tasteService.getMoodboard(cursorId, limit);

        // Then
        assertThat(moodboard.moodBoardResponseList()).hasSize(limit);
        assertThat(moodboard.moodBoardResponseList().get(0).id()).isEqualTo(30L);

        // 모든 ID가 내림차순으로 정렬되어야 함
        var ids = moodboard.moodBoardResponseList().stream()
                .map(MoodBoardResponse::id)
                .toList();
        for (int i = 0; i < ids.size() - 1; i++) {
            assertThat(ids.get(i)).isGreaterThan(ids.get(i + 1));
        }

    }
}