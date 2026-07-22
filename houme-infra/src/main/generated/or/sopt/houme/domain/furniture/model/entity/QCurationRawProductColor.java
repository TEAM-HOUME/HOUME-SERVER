package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCurationRawProductColor is a Querydsl query type for CurationRawProductColor
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCurationRawProductColor extends EntityPathBase<CurationRawProductColor> {

    private static final long serialVersionUID = -2131633860L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCurationRawProductColor curationRawProductColor = new QCurationRawProductColor("curationRawProductColor");

    public final StringPath clientColorName = createString("clientColorName");

    public final QCurationRawProduct curationRawProduct;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath rawColorName = createString("rawColorName");

    public QCurationRawProductColor(String variable) {
        this(CurationRawProductColor.class, forVariable(variable), INITS);
    }

    public QCurationRawProductColor(Path<? extends CurationRawProductColor> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCurationRawProductColor(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCurationRawProductColor(PathMetadata metadata, PathInits inits) {
        this(CurationRawProductColor.class, metadata, inits);
    }

    public QCurationRawProductColor(Class<? extends CurationRawProductColor> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.curationRawProduct = inits.isInitialized("curationRawProduct") ? new QCurationRawProduct(forProperty("curationRawProduct")) : null;
    }

}

