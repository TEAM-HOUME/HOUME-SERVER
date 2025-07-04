package or.sopt.houme.domain.house.entity;

import lombok.Getter;

@Getter
public enum Structure {
    OPEN_ONE_ROOM("오픈형 원룸"),
    SEPARATED_ONE_ROOM("분리형 원룸"),
    DUPLEX("복층형"),
    TWO_ROOM("투룸"),
    THREE_ROOM_OVER("쓰리룸+");

    private final String description;

    /**
     * 주어진 설명으로 구조(enum) 상수를 초기화합니다.
     *
     * @param description 구조 유형에 대한 한글 설명
     */
    Structure(String description) {
        this.description = description;
    }
}
