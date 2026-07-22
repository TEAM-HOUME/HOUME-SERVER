package or.sopt.houme.domain.house.model.floorPlan.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFloorPlan is a Querydsl query type for FloorPlan
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFloorPlan extends EntityPathBase<FloorPlan> {

    private static final long serialVersionUID = 1485331029L;

    public static final QFloorPlan floorPlan = new QFloorPlan("floorPlan");

    public final EnumPath<or.sopt.houme.domain.house.model.entity.enums.Equilibrium> equilibrium = createEnum("equilibrium", or.sopt.houme.domain.house.model.entity.enums.Equilibrium.class);

    public final StringPath equilibriumsJson = createString("equilibriumsJson");

    public final StringPath fileExtension = createString("fileExtension");

    public final StringPath filename = createString("filename");

    public final StringPath floorPlanName = createString("floorPlanName");

    public final StringPath floorPlanPrompt = createString("floorPlanPrompt");

    public final EnumPath<or.sopt.houme.domain.house.model.entity.enums.Form> form = createEnum("form", or.sopt.houme.domain.house.model.entity.enums.Form.class);

    public final StringPath formsJson = createString("formsJson");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imagesJson = createString("imagesJson");

    public final StringPath originalFilename = createString("originalFilename");

    public final EnumPath<or.sopt.houme.domain.house.model.entity.enums.Structure> structure = createEnum("structure", or.sopt.houme.domain.house.model.entity.enums.Structure.class);

    public final StringPath structuresJson = createString("structuresJson");

    public final StringPath url = createString("url");

    public QFloorPlan(String variable) {
        super(FloorPlan.class, forVariable(variable));
    }

    public QFloorPlan(Path<? extends FloorPlan> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFloorPlan(PathMetadata metadata) {
        super(FloorPlan.class, metadata);
    }

}

