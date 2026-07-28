package or.sopt.houme.domain.furniture.service;

/**
 * 가구 타입 마스터 캐시 전용 읽기 모델.
 *
 * {@code FurnitureType}은 다른 엔티티를 참조하지 않아 직렬화는 성공하지만,
 * 엔티티가 캐시 경계를 넘는 패턴 자체를 없애기 위해 함께 읽기 모델로 전환한다(#618).
 */
public record FurnitureTypeCacheView(Long id, String nameEng) {
}
