package or.sopt.houme.tastetag.infra.persistence;

import or.sopt.houme.tastetag.domain.TasteTag;

/**
 * 무드보드-태그 매핑 영속 엔티티 ↔ 순수 도메인 모델 매퍼.
 */
public final class TasteTagMapper {

    private TasteTagMapper() {
    }

    public static TasteTag toDomain(TasteTagJpaEntity entity) {
        return TasteTag.reconstitute(entity.getId(), entity.getTasteId(), entity.getTagId());
    }

    public static TasteTagJpaEntity toNewEntity(TasteTag tasteTag) {
        return TasteTagJpaEntity.forInsert(tasteTag.getTasteId(), tasteTag.getTagId());
    }
}
