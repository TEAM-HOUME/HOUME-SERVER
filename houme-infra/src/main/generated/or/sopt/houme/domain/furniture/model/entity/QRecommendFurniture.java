package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRecommendFurniture is a Querydsl query type for RecommendFurniture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecommendFurniture extends EntityPathBase<RecommendFurniture> {

    private static final long serialVersionUID = 210156225L;

    public static final QRecommendFurniture recommendFurniture = new QRecommendFurniture("recommendFurniture");

    public final NumberPath<Long> furnitureProductId = createNumber("furnitureProductId", Long.class);

    public final StringPath furnitureProductImageUrl = createString("furnitureProductImageUrl");

    public final StringPath furnitureProductMallName = createString("furnitureProductMallName");

    public final StringPath furnitureProductName = createString("furnitureProductName");

    public final StringPath furnitureProductSiteUrl = createString("furnitureProductSiteUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<CurationSource> source = createEnum("source", CurationSource.class);

    public QRecommendFurniture(String variable) {
        super(RecommendFurniture.class, forVariable(variable));
    }

    public QRecommendFurniture(Path<? extends RecommendFurniture> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRecommendFurniture(PathMetadata metadata) {
        super(RecommendFurniture.class, metadata);
    }

}

