package or.sopt.houme.taste.infra.persistence;

import or.sopt.houme.taste.domain.Taste;

/**
 * 취향 영속 엔티티 ↔ 순수 도메인 모델 매퍼.
 */
public final class TasteMapper {

    private TasteMapper() {
    }

    public static Taste toDomain(TasteJpaEntity entity) {
        return Taste.reconstitute(
                entity.getId(),
                entity.getUrl(),
                entity.getFilename(),
                entity.getOriginalFilename(),
                entity.getFileExtension()
        );
    }
}
