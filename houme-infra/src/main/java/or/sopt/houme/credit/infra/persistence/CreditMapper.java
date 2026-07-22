package or.sopt.houme.credit.infra.persistence;

import or.sopt.houme.credit.domain.Credit;

/**
 * 크레딧 영속 엔티티 ↔ 순수 도메인 모델 매퍼.
 */
final class CreditMapper {

    private CreditMapper() {
    }

    static Credit toDomain(CreditJpaEntity entity) {
        return Credit.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    static CreditJpaEntity toNewEntity(Credit credit) {
        return CreditJpaEntity.forInsert(credit.getUserId(), credit.getStatus());
    }
}
