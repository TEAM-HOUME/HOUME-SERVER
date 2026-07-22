package or.sopt.houme.carouseltag.infra.persistence;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCarouselTagJpaEntity is a Querydsl query type for CarouselTagJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCarouselTagJpaEntity extends EntityPathBase<CarouselTagJpaEntity> {

    private static final long serialVersionUID = -1846648880L;

    public static final QCarouselTagJpaEntity carouselTagJpaEntity = new QCarouselTagJpaEntity("carouselTagJpaEntity");

    public final NumberPath<Long> carouselId = createNumber("carouselId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> tagId = createNumber("tagId", Long.class);

    public QCarouselTagJpaEntity(String variable) {
        super(CarouselTagJpaEntity.class, forVariable(variable));
    }

    public QCarouselTagJpaEntity(Path<? extends CarouselTagJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCarouselTagJpaEntity(PathMetadata metadata) {
        super(CarouselTagJpaEntity.class, metadata);
    }

}

