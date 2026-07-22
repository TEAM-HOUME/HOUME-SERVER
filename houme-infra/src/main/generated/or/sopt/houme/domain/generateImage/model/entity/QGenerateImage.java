package or.sopt.houme.domain.generateImage.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGenerateImage is a Querydsl query type for GenerateImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGenerateImage extends EntityPathBase<GenerateImage> {

    private static final long serialVersionUID = 1320974567L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGenerateImage generateImage = new QGenerateImage("generateImage");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath fileExtension = createString("fileExtension");

    public final StringPath filename = createString("filename");

    public final EnumPath<GenerateImageType> generationType = createEnum("generationType", GenerateImageType.class);

    public final or.sopt.houme.house.infra.persistence.QHouseJpaEntity house;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath originalFilename = createString("originalFilename");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath url = createString("url");

    public QGenerateImage(String variable) {
        this(GenerateImage.class, forVariable(variable), INITS);
    }

    public QGenerateImage(Path<? extends GenerateImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGenerateImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGenerateImage(PathMetadata metadata, PathInits inits) {
        this(GenerateImage.class, metadata, inits);
    }

    public QGenerateImage(Class<? extends GenerateImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.house = inits.isInitialized("house") ? new or.sopt.houme.house.infra.persistence.QHouseJpaEntity(forProperty("house"), inits.get("house")) : null;
    }

}

