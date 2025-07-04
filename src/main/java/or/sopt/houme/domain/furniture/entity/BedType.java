package or.sopt.houme.domain.furniture.entity;

import lombok.Getter;

@Getter
public enum BedType {
    SINGLE("싱글"),
    SUPER_SINGLE("슈퍼싱글"),
    DOUBLE("더블"),
    QUEEN_OVER("퀸 이상"),
    MATTRESS_TOPPER("매트리스/토퍼");

    private final String description;

    /**
     * 각 침대 유형에 대한 설명을 초기화합니다.
     *
     * @param description 침대 유형의 한글 설명
     */
    BedType(String description) {
        this.description = description;
    }
}
