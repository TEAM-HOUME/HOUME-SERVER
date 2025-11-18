package or.sopt.houme.domain.house.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Activity {
    REMOTE_WORK("재택근무형"),
    READING("독서형"),
    FLOOR_LIVING("좌식 생활형"),
    HOME_CAFE("홈카페형");

    private final String description;
}
