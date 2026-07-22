package or.sopt.houme.domain.furniture.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QJjym is a Querydsl query type for Jjym
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJjym extends EntityPathBase<Jjym> {

    private static final long serialVersionUID = 965927871L;

    public static final QJjym jjym = new QJjym("jjym");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> recommendFurnitureId = createNumber("recommendFurnitureId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QJjym(String variable) {
        super(Jjym.class, forVariable(variable));
    }

    public QJjym(Path<? extends Jjym> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJjym(PathMetadata metadata) {
        super(Jjym.class, metadata);
    }

}

