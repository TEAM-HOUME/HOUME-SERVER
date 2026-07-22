package or.sopt.houme.house.infra.persistence;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHouseJpaEntity is a Querydsl query type for HouseJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHouseJpaEntity extends EntityPathBase<HouseJpaEntity> {

    private static final long serialVersionUID = -1032970588L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHouseJpaEntity houseJpaEntity = new QHouseJpaEntity("houseJpaEntity");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    public final EnumPath<or.sopt.houme.domain.house.model.entity.enums.Activity> activity = createEnum("activity", or.sopt.houme.domain.house.model.entity.enums.Activity.class);

    public final or.sopt.houme.domain.banner.model.entity.QBanner banner;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final ListPath<or.sopt.houme.domain.generateImage.model.entity.GenerateImage, or.sopt.houme.domain.generateImage.model.entity.QGenerateImage> generateImages = this.<or.sopt.houme.domain.generateImage.model.entity.GenerateImage, or.sopt.houme.domain.generateImage.model.entity.QGenerateImage>createList("generateImages", or.sopt.houme.domain.generateImage.model.entity.GenerateImage.class, or.sopt.houme.domain.generateImage.model.entity.QGenerateImage.class, PathInits.DIRECT2);

    public final ListPath<or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan, or.sopt.houme.domain.house.model.entity.mapping.QHouseFloorPlan> houseFloorPlans = this.<or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan, or.sopt.houme.domain.house.model.entity.mapping.QHouseFloorPlan>createList("houseFloorPlans", or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan.class, or.sopt.houme.domain.house.model.entity.mapping.QHouseFloorPlan.class, PathInits.DIRECT2);

    public final StringPath housePrompt = createString("housePrompt");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isValid = createBoolean("isValid");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QHouseJpaEntity(String variable) {
        this(HouseJpaEntity.class, forVariable(variable), INITS);
    }

    public QHouseJpaEntity(Path<? extends HouseJpaEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHouseJpaEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHouseJpaEntity(PathMetadata metadata, PathInits inits) {
        this(HouseJpaEntity.class, metadata, inits);
    }

    public QHouseJpaEntity(Class<? extends HouseJpaEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.banner = inits.isInitialized("banner") ? new or.sopt.houme.domain.banner.model.entity.QBanner(forProperty("banner"), inits.get("banner")) : null;
    }

}

