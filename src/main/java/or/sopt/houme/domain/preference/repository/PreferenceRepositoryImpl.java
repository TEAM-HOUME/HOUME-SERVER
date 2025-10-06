package or.sopt.houme.domain.preference.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.entity.QGenerateImage;
import or.sopt.houme.domain.house.entity.QHouse;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.entity.QGenerateImagePreference;
import or.sopt.houme.domain.preference.entity.QPreference;
import or.sopt.houme.domain.user.entity.QUser;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PreferenceRepositoryImpl implements PreferenceRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Preference> findPreferenceByUserIdAndImageId(Long userId, Long imageId) {
        QPreference preference = QPreference.preference;
        QGenerateImagePreference generateImagePreference = QGenerateImagePreference.generateImagePreference;
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QHouse house = QHouse.house;
        QUser user = QUser.user;

        return Optional.ofNullable(queryFactory
                .selectFrom(preference) // Preference 엔티티 선택
                .join(generateImagePreference).on(generateImagePreference.preference.eq(preference)).fetchJoin()
                .join(generateImage).on(generateImagePreference.generateImage.eq(generateImage)).fetchJoin()
                .join(house).on(generateImage.house.eq(house)).fetchJoin()
                .join(user).on(house.user.eq(user)).fetchJoin()
                .where(
                        user.id.eq(userId),
                        generateImage.id.eq(imageId)
                )
                .fetchOne());
    }
}
