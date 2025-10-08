package or.sopt.houme.domain.admin.controller.dto.furniture;

public record AdminFurniturePromptCreateResponseDTO(
        String presignedUrl,
        Long furnitureTagId
) {
}

