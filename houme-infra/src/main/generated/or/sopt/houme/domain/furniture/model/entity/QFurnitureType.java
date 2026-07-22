package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFurnitureType is a Querydsl query type for FurnitureType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFurnitureType extends EntityPathBase<FurnitureType> {

    private static final long serialVersionUID = 553198401L;

    public static final QFurnitureType furnitureType = new QFurnitureType("furnitureType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isRequired = createBoolean("isRequired");

    public final StringPath nameEng = createString("nameEng");

    public final StringPath nameKr = createString("nameKr");

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public QFurnitureType(String variable) {
        super(FurnitureType.class, forVariable(variable));
    }

    public QFurnitureType(Path<? extends FurnitureType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFurnitureType(PathMetadata metadata) {
        super(FurnitureType.class, metadata);
    }

}

