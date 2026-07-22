package or.sopt.houme.domain.preference.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPromptPreference is a Querydsl query type for PromptPreference
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPromptPreference extends EntityPathBase<PromptPreference> {

    private static final long serialVersionUID = 111654353L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPromptPreference promptPreference = new QPromptPreference("promptPreference");

    public final or.sopt.houme.house.infra.persistence.QHouseJpaEntity house;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPreference preference;

    public QPromptPreference(String variable) {
        this(PromptPreference.class, forVariable(variable), INITS);
    }

    public QPromptPreference(Path<? extends PromptPreference> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPromptPreference(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPromptPreference(PathMetadata metadata, PathInits inits) {
        this(PromptPreference.class, metadata, inits);
    }

    public QPromptPreference(Class<? extends PromptPreference> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.house = inits.isInitialized("house") ? new or.sopt.houme.house.infra.persistence.QHouseJpaEntity(forProperty("house"), inits.get("house")) : null;
        this.preference = inits.isInitialized("preference") ? new QPreference(forProperty("preference")) : null;
    }

}

