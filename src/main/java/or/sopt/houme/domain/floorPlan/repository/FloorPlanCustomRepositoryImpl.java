package or.sopt.houme.domain.floorPlan.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import org.springframework.stereotype.Repository;

import java.util.List;

import static or.sopt.houme.domain.floorPlan.entity.QFloorPlan.floorPlan;

@Repository
@RequiredArgsConstructor
public class FloorPlanCustomRepositoryImpl implements FloorPlanCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FloorPlan> findAllByStructureAndType(Form form, Structure structure) {

        return queryFactory
                .selectFrom(floorPlan)
                .where(
                        floorPlan.form.eq(form),
                        floorPlan.structure.eq(structure)
                )
                .fetch();
    }
}
