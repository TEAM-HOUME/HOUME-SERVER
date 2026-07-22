package or.sopt.houme.domain.generateImage.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QImageGenerationDetail is a Querydsl query type for ImageGenerationDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QImageGenerationDetail extends EntityPathBase<ImageGenerationDetail> {

    private static final long serialVersionUID = -21334523L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QImageGenerationDetail imageGenerationDetail = new QImageGenerationDetail("imageGenerationDetail");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> generatedImageId = createNumber("generatedImageId", Long.class);

    public final StringPath generatedImageUrl = createString("generatedImageUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QImageGenerationLog imageGenerationLog;

    public final NumberPath<Long> selectedStyleTagId = createNumber("selectedStyleTagId", Long.class);

    public final StringPath selectedStyleTagName = createString("selectedStyleTagName");

    public final StringPath selectionStrategy = createString("selectionStrategy");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QImageGenerationDetail(String variable) {
        this(ImageGenerationDetail.class, forVariable(variable), INITS);
    }

    public QImageGenerationDetail(Path<? extends ImageGenerationDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QImageGenerationDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QImageGenerationDetail(PathMetadata metadata, PathInits inits) {
        this(ImageGenerationDetail.class, metadata, inits);
    }

    public QImageGenerationDetail(Class<? extends ImageGenerationDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.imageGenerationLog = inits.isInitialized("imageGenerationLog") ? new QImageGenerationLog(forProperty("imageGenerationLog")) : null;
    }

}

