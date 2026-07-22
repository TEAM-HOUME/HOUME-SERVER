package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCurationFurniture is a Querydsl query type for CurationFurniture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCurationFurniture extends EntityPathBase<CurationFurniture> {

    private static final long serialVersionUID = 435609906L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCurationFurniture curationFurniture = new QCurationFurniture("curationFurniture");

    public final DateTimePath<java.time.LocalDateTime> fetchedAt = createDateTime("fetchedAt", java.time.LocalDateTime.class);

    public final QFurnitureTag furnitureTag;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> rank = createNumber("rank", Integer.class);

    public final QRecommendFurniture recommendFurniture;

    public final NumberPath<Double> similarity = createNumber("similarity", Double.class);

    public final EnumPath<CurationSource> source = createEnum("source", CurationSource.class);

    public QCurationFurniture(String variable) {
        this(CurationFurniture.class, forVariable(variable), INITS);
    }

    public QCurationFurniture(Path<? extends CurationFurniture> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCurationFurniture(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCurationFurniture(PathMetadata metadata, PathInits inits) {
        this(CurationFurniture.class, metadata, inits);
    }

    public QCurationFurniture(Class<? extends CurationFurniture> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.furnitureTag = inits.isInitialized("furnitureTag") ? new QFurnitureTag(forProperty("furnitureTag"), inits.get("furnitureTag")) : null;
        this.recommendFurniture = inits.isInitialized("recommendFurniture") ? new QRecommendFurniture(forProperty("recommendFurniture")) : null;
    }

}

