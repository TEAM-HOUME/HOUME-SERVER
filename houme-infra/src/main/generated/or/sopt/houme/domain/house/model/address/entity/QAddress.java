package or.sopt.houme.domain.house.model.address.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAddress is a Querydsl query type for Address
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAddress extends EntityPathBase<Address> {

    private static final long serialVersionUID = -2092065547L;

    public static final QAddress address = new QAddress("address");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath roadName = createString("roadName");

    public final StringPath sigungu = createString("sigungu");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QAddress(String variable) {
        super(Address.class, forVariable(variable));
    }

    public QAddress(Path<? extends Address> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAddress(PathMetadata metadata) {
        super(Address.class, metadata);
    }

}

