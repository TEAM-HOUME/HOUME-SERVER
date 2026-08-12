package or.sopt.houme.furniture.domain;

/** 가구 타입 read model. */
public record FurnitureTypeView(Long id, String nameKr, String nameEng, Boolean isRequired, Integer priority) {
}
