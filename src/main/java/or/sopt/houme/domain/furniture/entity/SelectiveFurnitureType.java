package or.sopt.houme.domain.furniture.entity;

import lombok.Getter;

@Getter
public enum SelectiveFurnitureType {
    DESK("책상"),
    MOVABLE_TV("이동식 TV"),
    DRAWER("서랍장"),
    TABLE_CHAIRS("식탁, 의자");

    private final String description;

    SelectiveFurnitureType(String description) {
        this.description = description;
    }
}
