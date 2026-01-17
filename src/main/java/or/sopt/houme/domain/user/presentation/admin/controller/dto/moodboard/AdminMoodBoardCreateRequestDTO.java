package or.sopt.houme.domain.user.presentation.admin.controller.dto.moodboard;

import java.util.List;

public record AdminMoodBoardCreateRequestDTO(
        String imageExtension,
        String originalFilename,
        Long tagId
) {
}
