package or.sopt.houme.compare.infra.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CurationProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<CurationRawProduct> findCandidatesByCategory(String category) {
        QCurationRawProduct p = QCurationRawProduct.curationRawProduct;

        SoozipCategory soozipCategory = null;
        if (category != null) {
            try {
                soozipCategory = SoozipCategory.valueOf(category);
            } catch (IllegalArgumentException ignored) {
                return List.of();
            }
        }

        var condition = p.imageEmbedding.isNotNull()
                .and(p.titleEmbedding.isNotNull())
                .and(p.isExposed.isTrue());

        if (soozipCategory != null) {
            condition = condition.and(p.category.eq(soozipCategory));
        }

        return queryFactory
                .selectFrom(p)
                .where(condition)
                .fetch();
    }
}
