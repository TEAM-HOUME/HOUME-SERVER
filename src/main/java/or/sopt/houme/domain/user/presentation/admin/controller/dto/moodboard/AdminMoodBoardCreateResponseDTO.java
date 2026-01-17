package or.sopt.houme.domain.user.presentation.admin.controller.dto.moodboard;

public record AdminMoodBoardCreateResponseDTO(
        String presignedUrl,
        Long tasteId
) {
}
