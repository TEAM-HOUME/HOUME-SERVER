package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCurationRawProductFurniture is a Querydsl query type for CurationRawProductFurniture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCurationRawProductFurniture extends EntityPathBase<CurationRawProductFurniture> {

    private static final long serialVersionUID = -47747765L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCurationRawProductFurniture curationRawProductFurniture = new QCurationRawProductFurniture("curationRawProductFurniture");

    public final QCurationRawProduct curationRawProduct;

    public final or.sopt.houme.furniture.infra.persistence.QFurnitureJpaEntity furniture;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QCurationRawProductFurniture(String variable) {
        this(CurationRawProductFurniture.class, forVariable(variable), INITS);
    }

    public QCurationRawProductFurniture(Path<? extends CurationRawProductFurniture> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCurationRawProductFurniture(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCurationRawProductFurniture(PathMetadata metadata, PathInits inits) {
        this(CurationRawProductFurniture.class, metadata, inits);
    }

    public QCurationRawProductFurniture(Class<? extends CurationRawProductFurniture> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.curationRawProduct = inits.isInitialized("curationRawProduct") ? new QCurationRawProduct(forProperty("curationRawProduct")) : null;
        this.furniture = inits.isInitialized("furniture") ? new or.sopt.houme.furniture.infra.persistence.QFurnitureJpaEntity(forProperty("furniture"), inits.get("furniture")) : null;
    }

}

