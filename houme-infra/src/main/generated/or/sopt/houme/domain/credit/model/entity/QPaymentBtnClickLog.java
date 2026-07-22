package or.sopt.houme.domain.credit.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPaymentBtnClickLog is a Querydsl query type for PaymentBtnClickLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentBtnClickLog extends EntityPathBase<PaymentBtnClickLog> {

    private static final long serialVersionUID = -353214782L;

    public static final QPaymentBtnClickLog paymentBtnClickLog = new QPaymentBtnClickLog("paymentBtnClickLog");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QPaymentBtnClickLog(String variable) {
        super(PaymentBtnClickLog.class, forVariable(variable));
    }

    public QPaymentBtnClickLog(Path<? extends PaymentBtnClickLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPaymentBtnClickLog(PathMetadata metadata) {
        super(PaymentBtnClickLog.class, metadata);
    }

}

