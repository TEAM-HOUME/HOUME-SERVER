package or.sopt.houme.domain.banner.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.model.entity.QBanner;
import or.sopt.houme.domain.banner.model.entity.QBannerCurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BannerRepositoryImpl implements BannerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Banner> findByIdWithRawProducts(Long bannerId, BannerType bannerType, boolean includeLegacyBanner) {
        QBanner banner = QBanner.banner;
        QBannerCurationRawProduct mapping = QBannerCurationRawProduct.bannerCurationRawProduct;
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;

        List<Banner> results = queryFactory
                .selectFrom(banner)
                .leftJoin(banner.bannerRawProducts, mapping).fetchJoin()
                .leftJoin(mapping.curationRawProduct, rawProduct).fetchJoin()
                .where(
                        banner.id.eq(bannerId),
                        bannerTypeCondition(banner, bannerType, includeLegacyBanner)
                )
                .fetch();

        return results.stream().findFirst();
    }

    @Override
    public List<Banner> findAllWithRawProducts(BannerType bannerType, boolean includeLegacyBanner) {
        QBanner banner = QBanner.banner;
        QBannerCurationRawProduct mapping = QBannerCurationRawProduct.bannerCurationRawProduct;
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;

        List<Banner> results = queryFactory
                .selectFrom(banner)
                .leftJoin(banner.bannerRawProducts, mapping).fetchJoin()
                .leftJoin(mapping.curationRawProduct, rawProduct).fetchJoin()
                .where(bannerTypeCondition(banner, bannerType, includeLegacyBanner))
                .orderBy(banner.id.desc(), mapping.id.asc())
                .fetch();

        Map<Long, Banner> distinctById = new LinkedHashMap<>();
        for (Banner result : results) {
            distinctById.putIfAbsent(result.getId(), result);
        }
        return List.copyOf(distinctById.values());
    }

    private BooleanBuilder bannerTypeCondition(QBanner banner, BannerType bannerType, boolean includeLegacyBanner) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.or(banner.bannerType.eq(bannerType));
        if (includeLegacyBanner) {
            condition.or(banner.bannerType.isNull());
        }
        return condition;
    }
}
