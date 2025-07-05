package or.sopt.houme.domain.house.entity.enums;

import lombok.Getter;

@Getter
public enum Activity {
    RELAXING("휴식형"),
    REMOTE_WORK("재택근무형"),
    HOME_THEATER("영화 감상형"),
    HOME_CAFE("홈카페형");

    private final String description;

    Activity(String description) {
        this.description = description;
    }
}
