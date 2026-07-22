package or.sopt.houme.domain.preference.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPreferenceFactor is a Querydsl query type for PreferenceFactor
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPreferenceFactor extends EntityPathBase<PreferenceFactor> {

    private static final long serialVersionUID = -1368227204L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPreferenceFactor preferenceFactor = new QPreferenceFactor("preferenceFactor");

    public final QFactor factor;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPreference preference;

    public QPreferenceFactor(String variable) {
        this(PreferenceFactor.class, forVariable(variable), INITS);
    }

    public QPreferenceFactor(Path<? extends PreferenceFactor> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPreferenceFactor(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPreferenceFactor(PathMetadata metadata, PathInits inits) {
        this(PreferenceFactor.class, metadata, inits);
    }

    public QPreferenceFactor(Class<? extends PreferenceFactor> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.factor = inits.isInitialized("factor") ? new QFactor(forProperty("factor")) : null;
        this.preference = inits.isInitialized("preference") ? new QPreference(forProperty("preference")) : null;
    }

}

