package or.sopt.houme.domain.preference.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFactor is a Querydsl query type for Factor
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFactor extends EntityPathBase<Factor> {

    private static final long serialVersionUID = -1877757887L;

    public static final QFactor factor = new QFactor("factor");

    public final StringPath factorText = createString("factorText");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isLike = createBoolean("isLike");

    public QFactor(String variable) {
        super(Factor.class, forVariable(variable));
    }

    public QFactor(Path<? extends Factor> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFactor(PathMetadata metadata) {
        super(Factor.class, metadata);
    }

}

