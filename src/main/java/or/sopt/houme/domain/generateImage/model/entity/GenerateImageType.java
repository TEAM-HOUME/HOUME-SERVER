package or.sopt.houme.domain.generateImage.model.entity;

import lombok.Getter;

@Getter
public enum GenerateImageType {
    LIST("목록형 이미지"),
    RECOMMEND("추천형 이미지");

    private final String description;

    GenerateImageType(String description) {
        this.description = description;
    }
}
