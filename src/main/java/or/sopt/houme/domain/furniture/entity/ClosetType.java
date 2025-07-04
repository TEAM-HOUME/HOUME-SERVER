package or.sopt.houme.domain.furniture.entity;

import lombok.Getter;

@Getter
public enum ClosetType {
    HANGER("행거형"),
    INDEPENDENT("독립형");

    private final String description;

    ClosetType(String description) {
        this.description = description;
    }
}
