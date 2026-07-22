package or.sopt.houme.tag.infra.persistence;

import or.sopt.houme.tag.domain.Tag;

/**
 * 태그 영속 엔티티 ↔ 순수 도메인 모델 매퍼.
 */
public final class TagMapper {

    private TagMapper() {
    }

    public static Tag toDomain(TagJpaEntity entity) {
        return Tag.reconstitute(
                entity.getId(),
                entity.getTagName(),
                entity.getPriority(),
                entity.getTagNameKr(),
                entity.getTagPrompt()
        );
    }

    public static TagJpaEntity toNewEntity(Tag tag) {
        return TagJpaEntity.forInsert(
                tag.getTagName(),
                tag.getPriority(),
                tag.getTagNameKr(),
                tag.getTagPrompt()
        );
    }
}
