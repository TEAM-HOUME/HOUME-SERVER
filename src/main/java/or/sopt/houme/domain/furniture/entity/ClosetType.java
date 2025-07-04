package or.sopt.houme.domain.furniture.entity;

import lombok.Getter;

@Getter
public enum ClosetType {
    HANGER("행거형"),
    INDEPENDENT("독립형");

    private final String description;

    /**
     * ClosetType 열거형 상수에 해당하는 설명 문자열을 초기화합니다.
     *
     * @param description 각 ClosetType 상수에 연결될 설명 문자열
     */
    ClosetType(String description) {
        this.description = description;
    }
}
