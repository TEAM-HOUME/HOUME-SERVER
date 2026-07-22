package or.sopt.houme.domain.banner.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBannerCurationRawProduct is a Querydsl query type for BannerCurationRawProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBannerCurationRawProduct extends EntityPathBase<BannerCurationRawProduct> {

    private static final long serialVersionUID = 1331063307L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBannerCurationRawProduct bannerCurationRawProduct = new QBannerCurationRawProduct("bannerCurationRawProduct");

    public final QBanner banner;

    public final or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct curationRawProduct;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QBannerCurationRawProduct(String variable) {
        this(BannerCurationRawProduct.class, forVariable(variable), INITS);
    }

    public QBannerCurationRawProduct(Path<? extends BannerCurationRawProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBannerCurationRawProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBannerCurationRawProduct(PathMetadata metadata, PathInits inits) {
        this(BannerCurationRawProduct.class, metadata, inits);
    }

    public QBannerCurationRawProduct(Class<? extends BannerCurationRawProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.banner = inits.isInitialized("banner") ? new QBanner(forProperty("banner"), inits.get("banner")) : null;
        this.curationRawProduct = inits.isInitialized("curationRawProduct") ? new or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct(forProperty("curationRawProduct")) : null;
    }

}

