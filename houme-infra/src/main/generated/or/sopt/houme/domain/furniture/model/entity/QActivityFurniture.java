package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QActivityFurniture is a Querydsl query type for ActivityFurniture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QActivityFurniture extends EntityPathBase<ActivityFurniture> {

    private static final long serialVersionUID = -1549003752L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QActivityFurniture activityFurniture = new QActivityFurniture("activityFurniture");

    public final EnumPath<or.sopt.houme.domain.house.model.entity.enums.Activity> activity = createEnum("activity", or.sopt.houme.domain.house.model.entity.enums.Activity.class);

    public final or.sopt.houme.furniture.infra.persistence.QFurnitureJpaEntity furniture;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public QActivityFurniture(String variable) {
        this(ActivityFurniture.class, forVariable(variable), INITS);
    }

    public QActivityFurniture(Path<? extends ActivityFurniture> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QActivityFurniture(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QActivityFurniture(PathMetadata metadata, PathInits inits) {
        this(ActivityFurniture.class, metadata, inits);
    }

    public QActivityFurniture(Class<? extends ActivityFurniture> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.furniture = inits.isInitialized("furniture") ? new or.sopt.houme.furniture.infra.persistence.QFurnitureJpaEntity(forProperty("furniture"), inits.get("furniture")) : null;
    }

}

