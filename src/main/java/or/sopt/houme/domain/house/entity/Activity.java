package or.sopt.houme.domain.house.entity;

import lombok.Getter;

@Getter
public enum Activity {
    RELAXING("휴식형"),
    REMOTE_WORK("재택근무형"),
    HOME_THEATER("영화 감상형"),
    HOME_CAFE("홈카페형");

    private final String description;

    /**
     * 각 활동 유형에 대한 설명을 초기화합니다.
     *
     * @param description 활동 유형의 한글 설명
     */
    Activity(String description) {
        this.description = description;
    }
}
