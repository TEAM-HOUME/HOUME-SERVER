package or.sopt.houme.furniture.domain;

import or.sopt.houme.domain.house.model.entity.enums.Activity;

/** 주요활동-가구 매핑 read model. */
public record ActivityFurnitureView(Activity activity, int priority, FurnitureWithTypeView furniture) {
}
