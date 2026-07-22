package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFurnitureRecommendBtnClickLog is a Querydsl query type for FurnitureRecommendBtnClickLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFurnitureRecommendBtnClickLog extends EntityPathBase<FurnitureRecommendBtnClickLog> {

    private static final long serialVersionUID = -111440189L;

    public static final QFurnitureRecommendBtnClickLog furnitureRecommendBtnClickLog = new QFurnitureRecommendBtnClickLog("furnitureRecommendBtnClickLog");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QFurnitureRecommendBtnClickLog(String variable) {
        super(FurnitureRecommendBtnClickLog.class, forVariable(variable));
    }

    public QFurnitureRecommendBtnClickLog(Path<? extends FurnitureRecommendBtnClickLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFurnitureRecommendBtnClickLog(PathMetadata metadata) {
        super(FurnitureRecommendBtnClickLog.class, metadata);
    }

}

