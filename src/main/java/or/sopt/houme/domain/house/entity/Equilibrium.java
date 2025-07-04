package or.sopt.houme.domain.house.entity;

import lombok.Getter;

@Getter
public enum Equilibrium {
    UNDER_5("5평 이하"),
    BETWEEN_6_10("6~10평"),
    BETWEEN_11_15("11~15평"),
    OVER_16("16평 이상");

    private final String description;

    Equilibrium(String description) {
        this.description = description;
    }
}
