package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.house.model.taste.entity.Tag;

public record AdminFurnitureTagOptionResponse(
        @Schema(description = "가구 태그 식별자")
        Long furnitureTagId,

        @Schema(description = "가구 식별자")
        Long furnitureId,

        @Schema(description = "가구 한글 이름")
        String furnitureNameKr,

        @Schema(description = "가구 타입 식별자")
        Long furnitureTypeId,

        @Schema(description = "가구 타입 한글 이름")
        String furnitureTypeNameKr,

        @Schema(description = "스타일 태그 식별자")
        Long tagId,

        @Schema(description = "스타일 태그 한글 이름")
        String tagNameKr,

        @Schema(description = "검색 키워드")
        String searchKeyword,

        @Schema(description = "우선순위")
        Integer priority
) {
    public static AdminFurnitureTagOptionResponse of(FurnitureTag furnitureTag) {
        Tag tag = furnitureTag.getTag();
        FurnitureType furnitureType = furnitureTag.getFurniture().getFurnitureType();

        return new AdminFurnitureTagOptionResponse(
                furnitureTag.getId(),
                furnitureTag.getFurniture().getId(),
                furnitureTag.getFurniture().getFurnitureNameKr(),
                furnitureType != null ? furnitureType.getId() : null,
                furnitureType != null ? furnitureType.getNameKr() : null,
                tag != null ? tag.getId() : null,
                tag != null ? tag.getTagNameKr() : null,
                furnitureTag.getSearchKeyword(),
                furnitureTag.getPriority()
        );
    }
}
