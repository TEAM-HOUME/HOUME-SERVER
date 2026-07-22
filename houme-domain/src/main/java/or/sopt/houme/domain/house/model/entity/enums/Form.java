package or.sopt.houme.domain.house.model.entity.enums;

import lombok.Getter;

@Getter
public enum Form {
    OFFICETEL("오피스텔"),
    VILLA("빌라/다세대"),
    APARTMENT("아파트"),
    ETC("그 외");

    private final String description;

    Form(String description) {
        this.description = description;
    }
}
