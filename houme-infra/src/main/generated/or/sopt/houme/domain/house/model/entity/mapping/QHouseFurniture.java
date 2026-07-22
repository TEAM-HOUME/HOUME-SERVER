package or.sopt.houme.domain.house.model.entity.mapping;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QHouseFurniture is a Querydsl query type for HouseFurniture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHouseFurniture extends EntityPathBase<HouseFurniture> {

    private static final long serialVersionUID = -864372437L;

    public static final QHouseFurniture houseFurniture = new QHouseFurniture("houseFurniture");

    public final NumberPath<Long> furnitureId = createNumber("furnitureId", Long.class);

    public final NumberPath<Long> houseId = createNumber("houseId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QHouseFurniture(String variable) {
        super(HouseFurniture.class, forVariable(variable));
    }

    public QHouseFurniture(Path<? extends HouseFurniture> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHouseFurniture(PathMetadata metadata) {
        super(HouseFurniture.class, metadata);
    }

}

