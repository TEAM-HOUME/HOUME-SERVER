package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCurationProductSearchKeyword is a Querydsl query type for CurationProductSearchKeyword
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCurationProductSearchKeyword extends EntityPathBase<CurationProductSearchKeyword> {

    private static final long serialVersionUID = -908889806L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCurationProductSearchKeyword curationProductSearchKeyword = new QCurationProductSearchKeyword("curationProductSearchKeyword");

    public final QCurationRawProduct curationRawProduct;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    public QCurationProductSearchKeyword(String variable) {
        this(CurationProductSearchKeyword.class, forVariable(variable), INITS);
    }

    public QCurationProductSearchKeyword(Path<? extends CurationProductSearchKeyword> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCurationProductSearchKeyword(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCurationProductSearchKeyword(PathMetadata metadata, PathInits inits) {
        this(CurationProductSearchKeyword.class, metadata, inits);
    }

    public QCurationProductSearchKeyword(Class<? extends CurationProductSearchKeyword> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.curationRawProduct = inits.isInitialized("curationRawProduct") ? new QCurationRawProduct(forProperty("curationRawProduct")) : null;
    }

}

