package or.sopt.houme.domain.banner.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBanner is a Querydsl query type for Banner
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBanner extends EntityPathBase<Banner> {

    private static final long serialVersionUID = 1076067695L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBanner banner = new QBanner("banner");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    public final StringPath bannerImageUrl = createString("bannerImageUrl");

    public final ListPath<BannerCurationRawProduct, QBannerCurationRawProduct> bannerRawProducts = this.<BannerCurationRawProduct, QBannerCurationRawProduct>createList("bannerRawProducts", BannerCurationRawProduct.class, QBannerCurationRawProduct.class, PathInits.DIRECT2);

    public final StringPath bannerTitle = createString("bannerTitle");

    public final EnumPath<BannerType> bannerType = createEnum("bannerType", BannerType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QBanner linkedBanner;

    public final StringPath styleAnswerChipsJson = createString("styleAnswerChipsJson");

    public final StringPath styleDescription = createString("styleDescription");

    public final StringPath stylePrompt = createString("stylePrompt");

    public final StringPath styleQuestion = createString("styleQuestion");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBanner(String variable) {
        this(Banner.class, forVariable(variable), INITS);
    }

    public QBanner(Path<? extends Banner> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBanner(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBanner(PathMetadata metadata, PathInits inits) {
        this(Banner.class, metadata, inits);
    }

    public QBanner(Class<? extends Banner> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.linkedBanner = inits.isInitialized("linkedBanner") ? new QBanner(forProperty("linkedBanner"), inits.get("linkedBanner")) : null;
    }

}

