package or.sopt.houme.domain.house.presentation.taste.dto.response;

import java.util.List;

public record MoodBoardListResponse(
        List<MoodBoardResponse> moodBoardResponseList
) {
    public static MoodBoardListResponse of(List<MoodBoardResponse> moodBoardResponseList) {
        return new MoodBoardListResponse(moodBoardResponseList);
    }
}
