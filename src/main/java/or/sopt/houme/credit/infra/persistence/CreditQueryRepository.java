package or.sopt.houme.credit.infra.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.credit.domain.CreditStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * FIFO 소진을 위한 "가장 오래된 크레딧" 조회 (QueryDSL). 기존 CreditCustomRepositoryImpl 로직 이관.
 */
@Repository
@RequiredArgsConstructor
public class CreditQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<CreditJpaEntity> findOldestByUserIdAndStatus(Long userId, CreditStatus status) {
        QCreditJpaEntity credit = QCreditJpaEntity.creditJpaEntity;

        return Optional.ofNullable(queryFactory
                .selectFrom(credit)
                .where(credit.userId.eq(userId).and(credit.status.eq(status)))
                .orderBy(credit.createdAt.asc())
                .fetchFirst());
    }
}
