package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCurationRawProductFurnitureTag is a Querydsl query type for CurationRawProductFurnitureTag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCurationRawProductFurnitureTag extends EntityPathBase<CurationRawProductFurnitureTag> {

    private static final long serialVersionUID = -819408305L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCurationRawProductFurnitureTag curationRawProductFurnitureTag = new QCurationRawProductFurnitureTag("curationRawProductFurnitureTag");

    public final QCurationRawProduct curationRawProduct;

    public final QFurnitureTag furnitureTag;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QCurationRawProductFurnitureTag(String variable) {
        this(CurationRawProductFurnitureTag.class, forVariable(variable), INITS);
    }

    public QCurationRawProductFurnitureTag(Path<? extends CurationRawProductFurnitureTag> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCurationRawProductFurnitureTag(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCurationRawProductFurnitureTag(PathMetadata metadata, PathInits inits) {
        this(CurationRawProductFurnitureTag.class, metadata, inits);
    }

    public QCurationRawProductFurnitureTag(Class<? extends CurationRawProductFurnitureTag> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.curationRawProduct = inits.isInitialized("curationRawProduct") ? new QCurationRawProduct(forProperty("curationRawProduct")) : null;
        this.furnitureTag = inits.isInitialized("furnitureTag") ? new QFurnitureTag(forProperty("furnitureTag"), inits.get("furnitureTag")) : null;
    }

}

