package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCurationRawProduct is a Querydsl query type for CurationRawProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCurationRawProduct extends EntityPathBase<CurationRawProduct> {

    private static final long serialVersionUID = -428454297L;

    public static final QCurationRawProduct curationRawProduct = new QCurationRawProduct("curationRawProduct");

    public final NumberPath<Long> baseShippingFee = createNumber("baseShippingFee", Long.class);

    public final StringPath brand = createString("brand");

    public final EnumPath<SoozipCategory> category = createEnum("category", SoozipCategory.class);

    public final NumberPath<Long> discountPrice = createNumber("discountPrice", Long.class);

    public final NumberPath<Integer> discountRate = createNumber("discountRate", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> fetchedAt = createDateTime("fetchedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> freeShippingCondition = createNumber("freeShippingCondition", Long.class);

    public final SetPath<CurationRawProductFurniture, QCurationRawProductFurniture> furnitureMappings = this.<CurationRawProductFurniture, QCurationRawProductFurniture>createSet("furnitureMappings", CurationRawProductFurniture.class, QCurationRawProductFurniture.class, PathInits.DIRECT2);

    public final SetPath<CurationRawProductFurnitureTag, QCurationRawProductFurnitureTag> furnitureTagMappings = this.<CurationRawProductFurnitureTag, QCurationRawProductFurnitureTag>createSet("furnitureTagMappings", CurationRawProductFurnitureTag.class, QCurationRawProductFurnitureTag.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isExposed = createBoolean("isExposed");

    public final NumberPath<Long> listPrice = createNumber("listPrice", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final StringPath productImageUrl = createString("productImageUrl");

    public final StringPath productMallName = createString("productMallName");

    public final StringPath productName = createString("productName");

    public final StringPath productSiteUrl = createString("productSiteUrl");

    public final StringPath searchTokens = createString("searchTokens");

    public final StringPath source = createString("source");

    public QCurationRawProduct(String variable) {
        super(CurationRawProduct.class, forVariable(variable));
    }

    public QCurationRawProduct(Path<? extends CurationRawProduct> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCurationRawProduct(PathMetadata metadata) {
        super(CurationRawProduct.class, metadata);
    }

}

