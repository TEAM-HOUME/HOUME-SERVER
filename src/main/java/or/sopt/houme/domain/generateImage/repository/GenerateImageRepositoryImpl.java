package or.sopt.houme.domain.generateImage.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.entity.QGenerateImage;
import or.sopt.houme.domain.house.entity.QHouse;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenerateImageRepositoryImpl implements GenerateImageRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<GenerateImage> findGenerateImageByUserIdAndImageId(Long userId, Long imageId) {
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QHouse house = QHouse.house;

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(generateImage)
                        .join(house).on(generateImage.house.eq(house))
                        .where(
                                house.user.id.eq(userId),
                                generateImage.id.eq(imageId)
                        )
                        .fetchOne()
        );
    }
}
