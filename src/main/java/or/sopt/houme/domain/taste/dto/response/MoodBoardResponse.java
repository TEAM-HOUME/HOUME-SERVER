package or.sopt.houme.domain.taste.dto.response;

import or.sopt.houme.domain.taste.entity.Taste;

public record MoodBoardResponse(
        Long id,
        String imageUrl,
        String fileExtension
) {

    public static MoodBoardResponse from(Taste taste) {
        return new MoodBoardResponse(
                taste.getId(),
                taste.getUrl(),
                taste.getFileExtension()
        );
    }
}
