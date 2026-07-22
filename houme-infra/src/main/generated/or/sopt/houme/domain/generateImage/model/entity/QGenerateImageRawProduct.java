package or.sopt.houme.domain.generateImage.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGenerateImageRawProduct is a Querydsl query type for GenerateImageRawProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGenerateImageRawProduct extends EntityPathBase<GenerateImageRawProduct> {

    private static final long serialVersionUID = -1446359058L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGenerateImageRawProduct generateImageRawProduct = new QGenerateImageRawProduct("generateImageRawProduct");

    public final or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct curationRawProduct;

    public final QGenerateImage generateImage;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QGenerateImageRawProduct(String variable) {
        this(GenerateImageRawProduct.class, forVariable(variable), INITS);
    }

    public QGenerateImageRawProduct(Path<? extends GenerateImageRawProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGenerateImageRawProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGenerateImageRawProduct(PathMetadata metadata, PathInits inits) {
        this(GenerateImageRawProduct.class, metadata, inits);
    }

    public QGenerateImageRawProduct(Class<? extends GenerateImageRawProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.curationRawProduct = inits.isInitialized("curationRawProduct") ? new or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct(forProperty("curationRawProduct")) : null;
        this.generateImage = inits.isInitialized("generateImage") ? new QGenerateImage(forProperty("generateImage"), inits.get("generateImage")) : null;
    }

}

