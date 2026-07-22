package or.sopt.houme.user.infra.persistence;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserJpaEntity is a Querydsl query type for UserJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserJpaEntity extends EntityPathBase<UserJpaEntity> {

    private static final long serialVersionUID = -735399954L;

    public static final QUserJpaEntity userJpaEntity = new QUserJpaEntity("userJpaEntity");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    public final DatePath<java.time.LocalDate> birthday = createDate("birthday", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final EnumPath<or.sopt.houme.domain.user.model.entity.Gender> gender = createEnum("gender", or.sopt.houme.domain.user.model.entity.Gender.class);

    public final BooleanPath hasGeneratedImage = createBoolean("hasGeneratedImage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath nickname = createString("nickname");

    public final StringPath nicknameTag = createString("nicknameTag");

    public final StringPath password = createString("password");

    public final EnumPath<or.sopt.houme.domain.user.model.entity.Role> role = createEnum("role", or.sopt.houme.domain.user.model.entity.Role.class);

    public final EnumPath<or.sopt.houme.domain.user.model.entity.SocialType> socialType = createEnum("socialType", or.sopt.houme.domain.user.model.entity.SocialType.class);

    public final EnumPath<or.sopt.houme.domain.user.model.entity.UserStatus> status = createEnum("status", or.sopt.houme.domain.user.model.entity.UserStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QUserJpaEntity(String variable) {
        super(UserJpaEntity.class, forVariable(variable));
    }

    public QUserJpaEntity(Path<? extends UserJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserJpaEntity(PathMetadata metadata) {
        super(UserJpaEntity.class, metadata);
    }

}

