package or.sopt.houme.domain.user.model.entity;

import lombok.Getter;

@Getter
public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    NONBINARY("논바이너리");

    private final String description;

    Gender(String description) {
        this.description = description;
    }
}
