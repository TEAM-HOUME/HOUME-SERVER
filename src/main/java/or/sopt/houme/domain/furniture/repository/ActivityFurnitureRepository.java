package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.ActivityFurniture;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityFurnitureRepository extends JpaRepository<ActivityFurniture, Long> {

    @EntityGraph(attributePaths = {"furniture", "furniture.furnitureType"})
    List<ActivityFurniture> findAllByActivityOrderByPriorityAscIdAsc(Activity activity);
}
