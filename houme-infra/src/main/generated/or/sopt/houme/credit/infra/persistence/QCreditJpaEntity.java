package or.sopt.houme.credit.infra.persistence;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCreditJpaEntity is a Querydsl query type for CreditJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCreditJpaEntity extends EntityPathBase<CreditJpaEntity> {

    private static final long serialVersionUID = -1923713362L;

    public static final QCreditJpaEntity creditJpaEntity = new QCreditJpaEntity("creditJpaEntity");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<or.sopt.houme.credit.domain.CreditStatus> status = createEnum("status", or.sopt.houme.credit.domain.CreditStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QCreditJpaEntity(String variable) {
        super(CreditJpaEntity.class, forVariable(variable));
    }

    public QCreditJpaEntity(Path<? extends CreditJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCreditJpaEntity(PathMetadata metadata) {
        super(CreditJpaEntity.class, metadata);
    }

}

