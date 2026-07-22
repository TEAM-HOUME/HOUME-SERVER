package or.sopt.houme.domain.preference.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCarouselPreference is a Querydsl query type for CarouselPreference
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCarouselPreference extends EntityPathBase<CarouselPreference> {

    private static final long serialVersionUID = 183737933L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCarouselPreference carouselPreference = new QCarouselPreference("carouselPreference");

    public final or.sopt.houme.carousel.infra.persistence.QCarouselJpaEntity carousel;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPreference preference;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QCarouselPreference(String variable) {
        this(CarouselPreference.class, forVariable(variable), INITS);
    }

    public QCarouselPreference(Path<? extends CarouselPreference> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCarouselPreference(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCarouselPreference(PathMetadata metadata, PathInits inits) {
        this(CarouselPreference.class, metadata, inits);
    }

    public QCarouselPreference(Class<? extends CarouselPreference> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.carousel = inits.isInitialized("carousel") ? new or.sopt.houme.carousel.infra.persistence.QCarouselJpaEntity(forProperty("carousel"), inits.get("carousel")) : null;
        this.preference = inits.isInitialized("preference") ? new QPreference(forProperty("preference")) : null;
    }

}

