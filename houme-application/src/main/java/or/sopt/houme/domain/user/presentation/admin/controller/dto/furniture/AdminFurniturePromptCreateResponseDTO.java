package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

public record AdminFurniturePromptCreateResponseDTO(
        String presignedUrl,
        Long furnitureTagId
) {
}

