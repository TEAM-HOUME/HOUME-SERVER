package or.sopt.houme.taste.infra.persistence;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTasteJpaEntity is a Querydsl query type for TasteJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTasteJpaEntity extends EntityPathBase<TasteJpaEntity> {

    private static final long serialVersionUID = -835275658L;

    public static final QTasteJpaEntity tasteJpaEntity = new QTasteJpaEntity("tasteJpaEntity");

    public final StringPath fileExtension = createString("fileExtension");

    public final StringPath filename = createString("filename");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath originalFilename = createString("originalFilename");

    public final StringPath url = createString("url");

    public QTasteJpaEntity(String variable) {
        super(TasteJpaEntity.class, forVariable(variable));
    }

    public QTasteJpaEntity(Path<? extends TasteJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTasteJpaEntity(PathMetadata metadata) {
        super(TasteJpaEntity.class, metadata);
    }

}

