package or.sopt.houme.domain.admin.controller.dto.moodboard;

public record AdminMoodBoardCreateResponseDTO(
        String presignedUrl,
        Long tasteId
) {
}
