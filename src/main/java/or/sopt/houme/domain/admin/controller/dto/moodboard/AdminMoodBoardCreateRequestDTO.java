package or.sopt.houme.domain.admin.controller.dto.moodboard;

public record AdminMoodBoardCreateRequestDTO(
        String imageExtension,
        String filename,
        String originalFilename,
        Long tagId
) {
}