package or.sopt.houme.tastetag.infra.persistence;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTasteTagJpaEntity is a Querydsl query type for TasteTagJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTasteTagJpaEntity extends EntityPathBase<TasteTagJpaEntity> {

    private static final long serialVersionUID = 1970030606L;

    public static final QTasteTagJpaEntity tasteTagJpaEntity = new QTasteTagJpaEntity("tasteTagJpaEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> tagId = createNumber("tagId", Long.class);

    public final NumberPath<Long> tasteId = createNumber("tasteId", Long.class);

    public QTasteTagJpaEntity(String variable) {
        super(TasteTagJpaEntity.class, forVariable(variable));
    }

    public QTasteTagJpaEntity(Path<? extends TasteTagJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTasteTagJpaEntity(PathMetadata metadata) {
        super(TasteTagJpaEntity.class, metadata);
    }

}

