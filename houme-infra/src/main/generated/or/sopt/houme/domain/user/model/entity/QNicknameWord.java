package or.sopt.houme.domain.user.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNicknameWord is a Querydsl query type for NicknameWord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNicknameWord extends EntityPathBase<NicknameWord> {

    private static final long serialVersionUID = -289516486L;

    public static final QNicknameWord nicknameWord = new QNicknameWord("nicknameWord");

    public final or.sopt.houme.global.entity.QBaseEntity _super = new or.sopt.houme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final EnumPath<NicknameWordType> type = createEnum("type", NicknameWordType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath word = createString("word");

    public QNicknameWord(String variable) {
        super(NicknameWord.class, forVariable(variable));
    }

    public QNicknameWord(Path<? extends NicknameWord> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNicknameWord(PathMetadata metadata) {
        super(NicknameWord.class, metadata);
    }

}

