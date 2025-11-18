package or.sopt.houme.domain.admin.controller.dto.furniture.type.response;

import io.swagger.v3.oas.annotations.media.Schema;
import or.sopt.houme.domain.furniture.entity.FurnitureType;

public record AdminFurnitureTypeResponse(
        @Schema(description = "가구 타입 식별자")
        Long furnitureTypeId,   // 가구 타입 식별자
        @Schema(description = "가구 타입 한글명")
        String furnitureTypeNameKr, // 가구 타입 한글명
        @Schema(description = "가구 타입 영어명")
        String furnitureTypeNameEng // 가구 타입 영어명
) {

    public static AdminFurnitureTypeResponse of(FurnitureType furnitureType){
        return new AdminFurnitureTypeResponse(furnitureType.getId(), furnitureType.getNameKr(), furnitureType.getNameEng());
    }
}
