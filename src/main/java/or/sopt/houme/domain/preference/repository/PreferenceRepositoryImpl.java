package or.sopt.houme.domain.preference.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.entity.QGenerateImage;
import or.sopt.houme.domain.house.entity.QHouse;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.entity.QGenerateImagePreference;
import or.sopt.houme.domain.preference.entity.QPreference;
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
        QHouse house = QHouse.house;
        QGenerateImage generateImage = QGenerateImage.generateImage;

        return Optional.ofNullable(queryFactory
                .selectFrom(preference)
                .join(preference).on(generateImagePreference.preference.eq(preference)).fetchJoin()
                .join(generateImage).on(generateImagePreference.generateImage.eq(generateImage)).fetchJoin()
                .join(generateImage).on(generateImage.house.eq(house)).fetchJoin()
                .where(
                        house.user.id.eq(userId),
                        generateImage.id.eq(imageId)
                ).fetchOne());
    }
}
