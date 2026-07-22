package or.sopt.houme.domain.preference.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPreference is a Querydsl query type for Preference
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPreference extends EntityPathBase<Preference> {

    private static final long serialVersionUID = 1073982157L;

    public static final QPreference preference = new QPreference("preference");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isLike = createBoolean("isLike");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QPreference(String variable) {
        super(Preference.class, forVariable(variable));
    }

    public QPreference(Path<? extends Preference> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPreference(PathMetadata metadata) {
        super(Preference.class, metadata);
    }

}

