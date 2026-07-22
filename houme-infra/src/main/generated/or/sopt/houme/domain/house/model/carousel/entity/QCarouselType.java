package or.sopt.houme.domain.house.model.carousel.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCarouselType is a Querydsl query type for CarouselType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCarouselType extends EntityPathBase<CarouselType> {

    private static final long serialVersionUID = -1443666711L;

    public static final QCarouselType carouselType = new QCarouselType("carouselType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath typeName = createString("typeName");

    public QCarouselType(String variable) {
        super(CarouselType.class, forVariable(variable));
    }

    public QCarouselType(Path<? extends CarouselType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCarouselType(PathMetadata metadata) {
        super(CarouselType.class, metadata);
    }

}

