package or.sopt.houme.domain.generateImage.model.entity;

import lombok.Getter;

@Getter
public enum GenerateImageType {
    BANNER("배너 기반 목록형 이미지"),
    STYLE("스타일 기반 목록형 이미지"),
    PRODUCT("상품 기반 목록형 이미지"),
    FULL_FUNNEL("추천형 이미지"),
    LEGACY("레거시 이미지 타입"),
    @Deprecated LIST("목록형 이미지"),
    @Deprecated RECOMMEND("추천형 이미지");

    private final String description;

    GenerateImageType(String description) {
        this.description = description;
    }
}
