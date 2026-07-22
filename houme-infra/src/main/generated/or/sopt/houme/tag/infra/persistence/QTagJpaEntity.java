package or.sopt.houme.tag.infra.persistence;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTagJpaEntity is a Querydsl query type for TagJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTagJpaEntity extends EntityPathBase<TagJpaEntity> {

    private static final long serialVersionUID = 1300453360L;

    public static final QTagJpaEntity tagJpaEntity = new QTagJpaEntity("tagJpaEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final StringPath tagName = createString("tagName");

    public final StringPath tagNameKr = createString("tagNameKr");

    public final StringPath tagPrompt = createString("tagPrompt");

    public QTagJpaEntity(String variable) {
        super(TagJpaEntity.class, forVariable(variable));
    }

    public QTagJpaEntity(Path<? extends TagJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTagJpaEntity(PathMetadata metadata) {
        super(TagJpaEntity.class, metadata);
    }

}

