package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;

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
    /**
     * #582: FurnitureTag→Tag 연관 절단으로 태그명은 tagId 로 별도 조회해 주입한다.
     * @param tagNameKr furnitureTag.getTagId() 로 조회한 태그 한글명(없으면 null)
     */
    public static AdminFurnitureTagOptionResponse of(FurnitureTag furnitureTag, String tagNameKr) {
        FurnitureType furnitureType = furnitureTag.getFurniture().getFurnitureType();

        return new AdminFurnitureTagOptionResponse(
                furnitureTag.getId(),
                furnitureTag.getFurniture().getId(),
                furnitureTag.getFurniture().getFurnitureNameKr(),
                furnitureType != null ? furnitureType.getId() : null,
                furnitureType != null ? furnitureType.getNameKr() : null,
                furnitureTag.getTagId(),
                tagNameKr,
                furnitureTag.getSearchKeyword(),
                furnitureTag.getPriority()
        );
    }
}
