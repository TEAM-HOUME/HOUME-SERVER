package or.sopt.houme.domain.furniture.service;

/**
 * 가구 마스터 캐시 전용 읽기 모델.
 *
 * JPA 엔티티({@code FurnitureJpaEntity})를 Redis에 그대로 직렬화하면
 * 가구 ↔ 가구태그 양방향 참조 때문에 무한 재귀로 저장이 항상 실패한다(#618).
 * 캐시 소비처가 실제로 쓰는 필드(id, nameEng)만 담아 순환이 캐시 경계를 넘지 않게 한다.
 */
public record FurnitureCacheView(Long id, String furnitureNameEng) {
}
