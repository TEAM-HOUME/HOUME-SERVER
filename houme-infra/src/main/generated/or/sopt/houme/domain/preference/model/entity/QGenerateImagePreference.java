package or.sopt.houme.domain.preference.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGenerateImagePreference is a Querydsl query type for GenerateImagePreference
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGenerateImagePreference extends EntityPathBase<GenerateImagePreference> {

    private static final long serialVersionUID = -785858129L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGenerateImagePreference generateImagePreference = new QGenerateImagePreference("generateImagePreference");

    public final or.sopt.houme.domain.generateImage.model.entity.QGenerateImage generateImage;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPreference preference;

    public QGenerateImagePreference(String variable) {
        this(GenerateImagePreference.class, forVariable(variable), INITS);
    }

    public QGenerateImagePreference(Path<? extends GenerateImagePreference> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGenerateImagePreference(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGenerateImagePreference(PathMetadata metadata, PathInits inits) {
        this(GenerateImagePreference.class, metadata, inits);
    }

    public QGenerateImagePreference(Class<? extends GenerateImagePreference> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.generateImage = inits.isInitialized("generateImage") ? new or.sopt.houme.domain.generateImage.model.entity.QGenerateImage(forProperty("generateImage"), inits.get("generateImage")) : null;
        this.preference = inits.isInitialized("preference") ? new QPreference(forProperty("preference")) : null;
    }

}

