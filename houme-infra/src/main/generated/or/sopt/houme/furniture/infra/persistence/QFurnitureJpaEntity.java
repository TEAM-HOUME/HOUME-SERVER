package or.sopt.houme.furniture.infra.persistence;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFurnitureJpaEntity is a Querydsl query type for FurnitureJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFurnitureJpaEntity extends EntityPathBase<FurnitureJpaEntity> {

    private static final long serialVersionUID = 1161688064L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFurnitureJpaEntity furnitureJpaEntity = new QFurnitureJpaEntity("furnitureJpaEntity");

    public final StringPath furnitureNameEng = createString("furnitureNameEng");

    public final StringPath furnitureNameKr = createString("furnitureNameKr");

    public final ListPath<or.sopt.houme.domain.furniture.model.entity.FurnitureTag, or.sopt.houme.domain.furniture.model.entity.QFurnitureTag> furnitureTags = this.<or.sopt.houme.domain.furniture.model.entity.FurnitureTag, or.sopt.houme.domain.furniture.model.entity.QFurnitureTag>createList("furnitureTags", or.sopt.houme.domain.furniture.model.entity.FurnitureTag.class, or.sopt.houme.domain.furniture.model.entity.QFurnitureTag.class, PathInits.DIRECT2);

    public final or.sopt.houme.domain.furniture.model.entity.QFurnitureType furnitureType;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath object365Word = createString("object365Word");

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public QFurnitureJpaEntity(String variable) {
        this(FurnitureJpaEntity.class, forVariable(variable), INITS);
    }

    public QFurnitureJpaEntity(Path<? extends FurnitureJpaEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFurnitureJpaEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFurnitureJpaEntity(PathMetadata metadata, PathInits inits) {
        this(FurnitureJpaEntity.class, metadata, inits);
    }

    public QFurnitureJpaEntity(Class<? extends FurnitureJpaEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.furnitureType = inits.isInitialized("furnitureType") ? new or.sopt.houme.domain.furniture.model.entity.QFurnitureType(forProperty("furnitureType")) : null;
    }

}

