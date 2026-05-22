package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;
import or.sopt.houme.domain.furniture.model.entity.Furniture;

public record AdminFurnitureOptionResponse(
        @Schema(description = "가구 ID")
        Long furnitureId,
        @Schema(description = "가구 한글 이름")
        String furnitureNameKr,
        @Schema(description = "가구 영어 이름")
        String furnitureNameEng
) {
    public static AdminFurnitureOptionResponse of(Furniture furniture) {
        return new AdminFurnitureOptionResponse(
                furniture.getId(),
                furniture.getFurnitureNameKr(),
                furniture.getFurnitureNameEng()
        );
    }
}
