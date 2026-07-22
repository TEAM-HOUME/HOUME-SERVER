package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFurnitureTag is a Querydsl query type for FurnitureTag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFurnitureTag extends EntityPathBase<FurnitureTag> {

    private static final long serialVersionUID = 1680412339L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFurnitureTag furnitureTag = new QFurnitureTag("furnitureTag");

    public final or.sopt.houme.furniture.infra.persistence.QFurnitureJpaEntity furniture;

    public final StringPath furniturePrompt = createString("furniturePrompt");

    public final StringPath furnitureUrl = createString("furnitureUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final StringPath searchKeyword = createString("searchKeyword");

    public final NumberPath<Long> tagId = createNumber("tagId", Long.class);

    public QFurnitureTag(String variable) {
        this(FurnitureTag.class, forVariable(variable), INITS);
    }

    public QFurnitureTag(Path<? extends FurnitureTag> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFurnitureTag(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFurnitureTag(PathMetadata metadata, PathInits inits) {
        this(FurnitureTag.class, metadata, inits);
    }

    public QFurnitureTag(Class<? extends FurnitureTag> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.furniture = inits.isInitialized("furniture") ? new or.sopt.houme.furniture.infra.persistence.QFurnitureJpaEntity(forProperty("furniture"), inits.get("furniture")) : null;
    }

}

