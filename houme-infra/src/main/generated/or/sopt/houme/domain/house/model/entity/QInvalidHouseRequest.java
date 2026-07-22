package or.sopt.houme.domain.house.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInvalidHouseRequest is a Querydsl query type for InvalidHouseRequest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInvalidHouseRequest extends EntityPathBase<InvalidHouseRequest> {

    private static final long serialVersionUID = 497194541L;

    public static final QInvalidHouseRequest invalidHouseRequest = new QInvalidHouseRequest("invalidHouseRequest");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<or.sopt.houme.domain.house.model.entity.enums.Equilibrium> equilibrium = createEnum("equilibrium", or.sopt.houme.domain.house.model.entity.enums.Equilibrium.class);

    public final EnumPath<or.sopt.houme.domain.house.model.entity.enums.Form> form = createEnum("form", or.sopt.houme.domain.house.model.entity.enums.Form.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<or.sopt.houme.domain.house.model.entity.enums.Structure> structure = createEnum("structure", or.sopt.houme.domain.house.model.entity.enums.Structure.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QInvalidHouseRequest(String variable) {
        super(InvalidHouseRequest.class, forVariable(variable));
    }

    public QInvalidHouseRequest(Path<? extends InvalidHouseRequest> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInvalidHouseRequest(PathMetadata metadata) {
        super(InvalidHouseRequest.class, metadata);
    }

}

