package or.sopt.houme.furniture.infra.persistence;

import or.sopt.houme.furniture.domain.Furniture;

/**
 * 가구 영속 엔티티 → 순수 도메인 모델 매퍼.
 *
 * <p>furnitureType 은 @ManyToOne(LAZY) 이지만 {@code getId()} 는 프록시 식별자 접근이라 추가 쿼리 없이 안전하다.
 */
final class FurnitureMapper {

    private FurnitureMapper() {
    }

    static Furniture toDomain(FurnitureJpaEntity entity) {
        return Furniture.reconstitute(
                entity.getId(),
                entity.getFurnitureNameEng(),
                entity.getFurnitureNameKr(),
                entity.getFurnitureType() != null ? entity.getFurnitureType().getId() : null,
                entity.getObject365Word(),
                entity.getPriority()
        );
    }
}
