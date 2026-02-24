package or.sopt.houme.domain.furniture.model.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum SoozipCategory {
    MINI_ELECTRONICS(73, "미니가전"),
    FURNITURE(75, "가구"),
    LIGHTING(76, "조명"),
    LIVING_GOODS(86, "생활용품"),
    HOME_FABRIC(47, "홈패브릭"),
    ACCESSORY(52, "소품");

    private final int cateNo;
    private final String label;

    public static Optional<SoozipCategory> fromCateNo(int cateNo) {
        return Arrays.stream(values())
                .filter(category -> category.cateNo == cateNo)
                .findFirst();
    }
}
