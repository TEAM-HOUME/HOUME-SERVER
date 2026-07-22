package or.sopt.houme.carousel.infra.persistence;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCarouselJpaEntity is a Querydsl query type for CarouselJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCarouselJpaEntity extends EntityPathBase<CarouselJpaEntity> {

    private static final long serialVersionUID = 518012430L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCarouselJpaEntity carouselJpaEntity = new QCarouselJpaEntity("carouselJpaEntity");

    public final or.sopt.houme.domain.house.model.carousel.entity.QCarouselType carouselType;

    public final StringPath fileExtension = createString("fileExtension");

    public final StringPath filename = createString("filename");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath originalFilename = createString("originalFilename");

    public final StringPath url = createString("url");

    public QCarouselJpaEntity(String variable) {
        this(CarouselJpaEntity.class, forVariable(variable), INITS);
    }

    public QCarouselJpaEntity(Path<? extends CarouselJpaEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCarouselJpaEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCarouselJpaEntity(PathMetadata metadata, PathInits inits) {
        this(CarouselJpaEntity.class, metadata, inits);
    }

    public QCarouselJpaEntity(Class<? extends CarouselJpaEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.carouselType = inits.isInitialized("carouselType") ? new or.sopt.houme.domain.house.model.carousel.entity.QCarouselType(forProperty("carouselType")) : null;
    }

}

