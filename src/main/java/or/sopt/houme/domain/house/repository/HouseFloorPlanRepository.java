package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseFloorPlanRepository extends JpaRepository<HouseFloorPlan, Long> {

    @Query("SELECT hfp FROM HouseFloorPlan hfp WHERE hfp.house.id = :houseId")
    Optional<HouseFloorPlan> findHouseFloorPlanByHouseId(@Param("houseId") Long houseId);

    @Query("SELECT hfp FROM HouseFloorPlan hfp WHERE hfp.house.id IN :houseIds")
    List<HouseFloorPlan> findAllByHouseIdIn(@Param("houseIds") List<Long> houseIds);

    void deleteByHouseId(Long houseId);
}
