package or.sopt.houme.domain.preference.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.preference.entity.Factor;
import or.sopt.houme.domain.preference.entity.QFactor;
import or.sopt.houme.domain.preference.entity.QPreferenceFactor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FactorRepositoryImpl implements FactorRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Factor> findFactorsByIsLike(boolean isLike) {
        QFactor factor = QFactor.factor;

        return queryFactory
                .selectFrom(factor)
                .where(factor.isLike.eq(isLike))
                .fetch();
    }

    // preferenceId로 PreferenceFactor찾고, 있으면 PreferenceFactor 삭제
    @Override
    public void findPreferenceFactorAndDeleteByPreferenceId(Long preferenceId) {
        QPreferenceFactor preferenceFactor = QPreferenceFactor.preferenceFactor;

        queryFactory.delete(preferenceFactor)
                .where(preferenceFactor.preference.id.eq(preferenceId))
                .execute();
    }
}
