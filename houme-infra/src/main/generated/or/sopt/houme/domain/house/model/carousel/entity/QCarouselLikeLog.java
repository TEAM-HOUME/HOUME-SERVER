package or.sopt.houme.domain.house.model.carousel.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCarouselLikeLog is a Querydsl query type for CarouselLikeLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCarouselLikeLog extends EntityPathBase<CarouselLikeLog> {

    private static final long serialVersionUID = -1740154850L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCarouselLikeLog carouselLikeLog = new QCarouselLikeLog("carouselLikeLog");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    public final EnumPath<CarouselLikeLogAction> action = createEnum("action", CarouselLikeLogAction.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct curationRawProduct;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QCarouselLikeLog(String variable) {
        this(CarouselLikeLog.class, forVariable(variable), INITS);
    }

    public QCarouselLikeLog(Path<? extends CarouselLikeLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCarouselLikeLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCarouselLikeLog(PathMetadata metadata, PathInits inits) {
        this(CarouselLikeLog.class, metadata, inits);
    }

    public QCarouselLikeLog(Class<? extends CarouselLikeLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.curationRawProduct = inits.isInitialized("curationRawProduct") ? new or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct(forProperty("curationRawProduct")) : null;
    }

}

