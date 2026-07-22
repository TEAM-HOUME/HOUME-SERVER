package or.sopt.houme.domain.house.model.entity.mapping;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHouseFloorPlan is a Querydsl query type for HouseFloorPlan
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHouseFloorPlan extends EntityPathBase<HouseFloorPlan> {

    private static final long serialVersionUID = -1996448114L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHouseFloorPlan houseFloorPlan = new QHouseFloorPlan("houseFloorPlan");

    public final or.sopt.houme.domain.house.model.floorPlan.entity.QFloorPlan floorPlan;

    public final or.sopt.houme.house.infra.persistence.QHouseJpaEntity house;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isReverse = createBoolean("isReverse");

    public final StringPath selectedView = createString("selectedView");

    public QHouseFloorPlan(String variable) {
        this(HouseFloorPlan.class, forVariable(variable), INITS);
    }

    public QHouseFloorPlan(Path<? extends HouseFloorPlan> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHouseFloorPlan(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHouseFloorPlan(PathMetadata metadata, PathInits inits) {
        this(HouseFloorPlan.class, metadata, inits);
    }

    public QHouseFloorPlan(Class<? extends HouseFloorPlan> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.floorPlan = inits.isInitialized("floorPlan") ? new or.sopt.houme.domain.house.model.floorPlan.entity.QFloorPlan(forProperty("floorPlan")) : null;
        this.house = inits.isInitialized("house") ? new or.sopt.houme.house.infra.persistence.QHouseJpaEntity(forProperty("house"), inits.get("house")) : null;
    }

}

