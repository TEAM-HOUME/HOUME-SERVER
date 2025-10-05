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
        QHouse house = QHouse.house;
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QUser user = QUser.user;

        return Optional.ofNullable(queryFactory
                .select(preference) // Preference 엔티티를 선택
                .from(generateImagePreference) // generateImagePreference에서 시작
                .join(generateImagePreference.preference, preference).fetchJoin() // Preference 페치 조인
                .join(generateImagePreference.generateImage, generateImage).fetchJoin() // GenerateImage 페치 조인
                .join(generateImage.house, house).fetchJoin() // House 페치 조인
                .join(house.user, user).fetchJoin() // User 페치 조인 (User 정보가 필요하다면)
                .where(
                        user.id.eq(userId), // House의 User ID 조건
                        generateImage.id.eq(imageId) // GenerateImage ID 조건
                )
                .fetchOne());
    }
}
