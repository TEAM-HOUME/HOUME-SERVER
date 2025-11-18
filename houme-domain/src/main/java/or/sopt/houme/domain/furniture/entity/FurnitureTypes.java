package or.sopt.houme.domain.furniture.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FurnitureTypes {
    BED("침대"),
    SELECTIVE("선택가구");

    private final String description;
}
