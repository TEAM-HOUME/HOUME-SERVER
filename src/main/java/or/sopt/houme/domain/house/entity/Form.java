package or.sopt.houme.domain.house.entity;

import lombok.Getter;

@Getter
public enum Form {
    OFFICETEL("오피스텔"),
    VILLA("빌라/다세대"),
    APARTMENT("아파트"),
    ETC("그 외");

    private final String description;

    /**
     * 주어진 설명 문자열로 Form 열거형 상수를 초기화합니다.
     *
     * @param description 주택 형태에 대한 설명 문자열
     */
    Form(String description) {
        this.description = description;
    }
}
