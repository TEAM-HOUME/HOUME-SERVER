package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.model.entity.mapping.HouseFurniture;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseFurnitureRepository extends JpaRepository<HouseFurniture, Long> {
    void deleteByHouseId(Long houseId);

    @Query("""
            select houseFurniture
            from HouseFurniture houseFurniture
            join fetch houseFurniture.furniture furniture
            join fetch furniture.furnitureType
            where houseFurniture.house.id = :houseId
            order by houseFurniture.id asc
            """)
    List<HouseFurniture> findAllByHouseIdWithFurniture(@Param("houseId") Long houseId);
}
