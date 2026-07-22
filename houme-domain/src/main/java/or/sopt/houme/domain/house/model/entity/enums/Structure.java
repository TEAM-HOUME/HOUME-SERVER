package or.sopt.houme.domain.house.model.entity.enums;

import lombok.Getter;

@Getter
public enum Structure {
    OPEN_ONE_ROOM("오픈형 원룸"),
    SEPARATED_ONE_ROOM("분리형 원룸"),
    DUPLEX("복층형"),
    TWO_ROOM("투룸"),
    THREE_ROOM_OVER("쓰리룸+");

    private final String description;

    Structure(String description) {
        this.description = description;
    }
}
