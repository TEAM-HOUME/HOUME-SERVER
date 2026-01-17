package or.sopt.houme.domain.house.service.taste;

import or.sopt.houme.domain.house.presentation.taste.dto.response.MoodBoardListResponse;
import or.sopt.houme.domain.house.presentation.taste.dto.response.MoodBoardResponse;
import or.sopt.houme.domain.house.model.taste.entity.Taste;
import or.sopt.houme.domain.house.repository.taste.taste.TasteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("[Taste Service] Test")
class TasteServiceImplTest {

    @Mock
    TasteRepository tasteRepository;

    @InjectMocks
    TasteServiceImpl tasteService;

    @Test
    @DisplayName("cursor 기반으로 페이지네이션을 구현 할 수 있다.")
    void getMoodBoardWithCursor() {
        // Given
        String url = "http://localhost:8080";
        String fileName = "fileName";
        String originalFilename = "originalFilename";
        String fileExtension = "jpg";

        List<Taste> tastes = new ArrayList<>();

        for (int i = 14; i > 0; i--) {
            tastes.add(Taste.builder()
                            .id((long)i)
                    .url(url + i)
                    .filename(fileName + i)
                    .originalFilename(originalFilename + i)
                    .fileExtension(fileExtension)
                    .build());
        }

        Long cursorId = 15L;
        int limit = 14;

        when(tasteRepository.findAll()).thenReturn(tastes);

        // When
        MoodBoardListResponse moodboard = tasteService.getMoodboard(cursorId, limit);

        // Then
        assertThat(moodboard.moodBoardResponseList()).hasSize(limit);

        // 무드보드 리스트의 첫 번째 요소의 ID가 cursorId보다 작아야 함
        Long firstId = moodboard.moodBoardResponseList().get(0).id();
        assertThat(firstId).isLessThan(cursorId);
    }

    @Test
    @DisplayName("cursor가 null이면 최신순을 기반으로 페이지네이션을 구현 할 수 있다.")
    void getMoodBoardWithCursorNull() {
        // Given
        String url = "http://localhost:8080";
        String fileName = "fileName";
        String originalFilename = "originalFilename";
        String fileExtension = "jpg";

        List<Taste> tastes = new ArrayList<>();

        for (int i = 30; i > 0; i--) {
            tastes.add(Taste.builder()
                            .id((long) i)
                    .url(url + i)
                    .filename(fileName + i)
                    .originalFilename(originalFilename + i)
                    .fileExtension(fileExtension)
                    .build());
        }

        Long cursorId = null;
        int limit = 10;
        int size = 30;

        when(tasteRepository.findAll()).thenReturn(tastes);

        // When
        MoodBoardListResponse moodboard = tasteService.getMoodboard(cursorId, limit);

        // Then
        assertThat(moodboard.moodBoardResponseList()).hasSize(size);
        assertThat(moodboard.moodBoardResponseList()).isNotNull();

    }
}