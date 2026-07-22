package or.sopt.houme.domain.house.model.entity.mapping;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QHouseTaste is a Querydsl query type for HouseTaste
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHouseTaste extends EntityPathBase<HouseTaste> {

    private static final long serialVersionUID = -277913008L;

    public static final QHouseTaste houseTaste = new QHouseTaste("houseTaste");

    public final NumberPath<Long> houseId = createNumber("houseId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> tasteId = createNumber("tasteId", Long.class);

    public QHouseTaste(String variable) {
        super(HouseTaste.class, forVariable(variable));
    }

    public QHouseTaste(Path<? extends HouseTaste> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHouseTaste(PathMetadata metadata) {
        super(HouseTaste.class, metadata);
    }

}

