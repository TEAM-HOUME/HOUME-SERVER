package or.sopt.houme.domain.admin.controller.dto.moodboard;

import java.util.List;

public record AdminMoodBoardCreateRequestDTO(
        String imageExtension,
        String originalFilename,
        Long tagId
) {
}
