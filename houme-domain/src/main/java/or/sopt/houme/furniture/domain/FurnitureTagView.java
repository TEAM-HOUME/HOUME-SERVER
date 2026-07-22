package or.sopt.houme.furniture.domain;

/** 가구-스타일태그 매핑 read model (가구 요약 평탄화 포함). */
public record FurnitureTagView(
        Long id,
        String furniturePrompt,
        Long furnitureId,
        String furnitureNameKr,
        Long tagId,
        String furnitureUrl,
        String searchKeyword,
        Integer priority
) {
}
