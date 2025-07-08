package or.sopt.houme.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.credit.entity.CreditStatus;
import org.springframework.stereotype.Repository;

import static or.sopt.houme.domain.credit.entity.QCredit.credit;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public long countByMemberIdAndStatus(Long userId) {
        return queryFactory
                .select(credit.count())
                .from(credit)
                .where(
                        credit.id.eq(userId),
                        credit.status.eq(CreditStatus.ACTIVE)
                )
                .fetchOne();
    }
}
