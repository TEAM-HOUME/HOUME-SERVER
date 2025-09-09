package or.sopt.houme.domain.credit.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.entity.CreditStatus;
import or.sopt.houme.domain.credit.entity.QCredit;
import or.sopt.houme.domain.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CreditCustomRepositoryImpl implements CreditCustomRepository {

    private final JPAQueryFactory queryFactory;

    // User로 Credit 찾기
    @Override
    public Optional<Credit> findOldCreditByUser(User user) {
        QCredit credit = QCredit.credit;

        return Optional.ofNullable(queryFactory.selectFrom(credit)
                .where(credit.user.eq(user).and(credit.status.eq(CreditStatus.ACTIVE)))
                .orderBy(credit.createdAt.asc())
                .fetchOne());
    }

    // User와 Status로 제일 오래된 Credit 찾기
    @Override
    public Optional<Credit> findOldCreditByUserAndStatus(User user, CreditStatus status) {
        QCredit credit = QCredit.credit;

        return Optional.ofNullable(queryFactory
                .selectFrom(credit)
                .where(credit.user.eq(user)
                        .and(credit.status.eq(CreditStatus.ACTIVE))
                )
                .orderBy(credit.createdAt.asc())
                .fetchOne());
    }
}
