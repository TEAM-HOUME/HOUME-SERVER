package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

public record AdminFurnitureUpdateResponseDTO(
        String presignedUrl,
        Long furnitureTagId
) {
    public static AdminFurnitureUpdateResponseDTO of(String presignedUrl, Long furnitureTagId) {
        return new AdminFurnitureUpdateResponseDTO(presignedUrl, furnitureTagId);
    }
}

