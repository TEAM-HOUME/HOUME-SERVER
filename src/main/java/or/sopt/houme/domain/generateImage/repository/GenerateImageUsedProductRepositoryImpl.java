package or.sopt.houme.domain.generateImage.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageUsedProduct;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImageUsedProduct;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenerateImageUsedProductRepositoryImpl implements GenerateImageUsedProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GenerateImageUsedProduct> findAllByGenerateImageIdInWithRawProduct(List<Long> generateImageIds) {
        if (generateImageIds == null || generateImageIds.isEmpty()) {
            return List.of();
        }

        QGenerateImageUsedProduct mapping = QGenerateImageUsedProduct.generateImageUsedProduct;
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
