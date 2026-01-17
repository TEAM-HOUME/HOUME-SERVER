package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AdminFurnitureGetDTO(


        List<FurnitureInfo> furnitures
) {
    public record FurnitureInfo(
            @Schema(description = "가구 ID")
            Long furnitureId,
            @Schema(description = "가구 한글 이름")
            String furnitureNameKr,
            @Schema(description = "연결된 태그 정보 리스트")
            List<TagInfo> tags
    ) {}

    public record TagInfo(
            @Schema(description = "가구-태그 매핑 ID (FurnitureTag ID)")
            Long furnitureTagId,

            @Schema(description = "태그 ID")
            Long tagId,

            @Schema(description = "태그 이름")
            String tagName,

            @Schema(description = "가구 대표 이미지 URL")
            String imageUrl,

            @Schema(description = "검색 키워드")
            String searchKeyword,

            @Schema(description = "우선순위")
            Integer priority
    ) {}
}

