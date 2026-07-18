package or.sopt.houme.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImage;
import or.sopt.houme.house.infra.persistence.QHouseJpaEntity;
import or.sopt.houme.domain.user.model.entity.QUser;
import or.sopt.houme.domain.user.model.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<GenerateImage> findImageHistoryById(Long userId) {
        QUser user = QUser.user;
        QHouseJpaEntity house = QHouseJpaEntity.houseJpaEntity;
        QGenerateImage generateImage = QGenerateImage.generateImage;

        return Optional.ofNullable(queryFactory
                .select(generateImage)
                .from(house)
                .join(house.generateImages, generateImage)
                .join(house.user, user)
                .where(user.id.eq(userId))
                .limit(1)
                .fetchOne());
    }

    @Override
    public List<User> searchMembers(String keyword, int limit) {
        QUser user = QUser.user;
        return queryFactory
                .selectFrom(user)
                .where(user.email.eq(keyword)
                        .or(user.nickname.containsIgnoreCase(keyword)))
                .orderBy(user.id.asc())
                .limit(limit)
                .fetch();
    }
}
