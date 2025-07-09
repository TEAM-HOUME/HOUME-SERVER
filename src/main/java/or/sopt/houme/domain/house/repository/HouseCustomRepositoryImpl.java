package or.sopt.houme.domain.house.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.QHouse;
import or.sopt.houme.domain.user.entity.User;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HouseCustomRepositoryImpl implements HouseCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public House findLatestHouse(User user) {
        QHouse qHouse = QHouse.house;

        return queryFactory
                .selectFrom(qHouse)
                .where(qHouse.user.eq(user))
                .orderBy(qHouse.id.desc())
                .limit(1)
                .fetchOne();
    }
}
