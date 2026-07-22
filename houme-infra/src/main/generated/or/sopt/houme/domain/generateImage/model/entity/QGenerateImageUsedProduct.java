package or.sopt.houme.domain.generateImage.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGenerateImageUsedProduct is a Querydsl query type for GenerateImageUsedProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGenerateImageUsedProduct extends EntityPathBase<GenerateImageUsedProduct> {

    private static final long serialVersionUID = 725459723L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGenerateImageUsedProduct generateImageUsedProduct = new QGenerateImageUsedProduct("generateImageUsedProduct");

    public final or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct curationRawProduct;

    public final QGenerateImage generateImage;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QGenerateImageUsedProduct(String variable) {
        this(GenerateImageUsedProduct.class, forVariable(variable), INITS);
    }

    public QGenerateImageUsedProduct(Path<? extends GenerateImageUsedProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGenerateImageUsedProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGenerateImageUsedProduct(PathMetadata metadata, PathInits inits) {
        this(GenerateImageUsedProduct.class, metadata, inits);
    }

    public QGenerateImageUsedProduct(Class<? extends GenerateImageUsedProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.curationRawProduct = inits.isInitialized("curationRawProduct") ? new or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct(forProperty("curationRawProduct")) : null;
        this.generateImage = inits.isInitialized("generateImage") ? new QGenerateImage(forProperty("generateImage"), inits.get("generateImage")) : null;
    }

}

