package or.sopt.houme.domain.generateImage.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QImageGenerationLog is a Querydsl query type for ImageGenerationLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QImageGenerationLog extends EntityPathBase<ImageGenerationLog> {

    private static final long serialVersionUID = -1786691056L;

    public static final QImageGenerationLog imageGenerationLog = new QImageGenerationLog("imageGenerationLog");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> generatedImageCount = createNumber("generatedImageCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<ImageGenerationDetail, QImageGenerationDetail> imageGenerationDetailList = this.<ImageGenerationDetail, QImageGenerationDetail>createList("imageGenerationDetailList", ImageGenerationDetail.class, QImageGenerationDetail.class, PathInits.DIRECT2);

    public final NumberPath<Integer> selectedMoodboardCount = createNumber("selectedMoodboardCount", Integer.class);

    public final StringPath selectedMoodboardIds = createString("selectedMoodboardIds");

    public final StringPath selectedMoodboardNames = createString("selectedMoodboardNames");

    public final StringPath selectedStyleTagIds = createString("selectedStyleTagIds");

    public final StringPath selectedStyleTagNames = createString("selectedStyleTagNames");

    public final StringPath type = createString("type");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QImageGenerationLog(String variable) {
        super(ImageGenerationLog.class, forVariable(variable));
    }

    public QImageGenerationLog(Path<? extends ImageGenerationLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QImageGenerationLog(PathMetadata metadata) {
        super(ImageGenerationLog.class, metadata);
    }

}

