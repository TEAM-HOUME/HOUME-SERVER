package or.sopt.houme.domain.generateImage.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageRawProduct;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImageRawProduct;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenerateImageRawProductRepositoryImpl implements GenerateImageRawProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GenerateImageRawProduct> findAllByGenerateImageIdWithRawProduct(Long generateImageId) {
        if (generateImageId == null) {
            return List.of();
        }

        return findAllByGenerateImageIdInWithRawProduct(List.of(generateImageId));
    }

    @Override
    public List<GenerateImageRawProduct> findAllByGenerateImageIdInWithRawProduct(List<Long> generateImageIds) {
        if (generateImageIds == null || generateImageIds.isEmpty()) {
            return List.of();
        }

        QGenerateImageRawProduct mapping = QGenerateImageRawProduct.generateImageRawProduct;
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;

        return queryFactory
                .selectFrom(mapping)
                .join(mapping.generateImage, generateImage).fetchJoin()
                .join(mapping.curationRawProduct, rawProduct).fetchJoin()
                .where(generateImage.id.in(generateImageIds))
                .orderBy(generateImage.id.asc(), mapping.sortOrder.asc(), mapping.id.asc())
                .fetch();
    }
}
