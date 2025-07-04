package or.sopt.houme.domain.furniture.entity;

import lombok.Getter;

@Getter
public enum SelectiveFurnitureType {
    DESK("책상"),
    MOVABLE_TV("이동식 TV"),
    DRAWER("서랍장"),
    TABLE_CHAIRS("식탁, 의자");

    private final String description;

    /**
     * 각 가구 유형에 대한 설명을 초기화합니다.
     *
     * @param description 가구 유형의 한글 설명
     */
    SelectiveFurnitureType(String description) {
        this.description = description;
    }
}
