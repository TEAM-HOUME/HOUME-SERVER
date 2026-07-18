package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.model.entity.mapping.HouseTaste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseTasteRepository extends JpaRepository<HouseTaste, Long> {
    void deleteByHouseId(Long houseId);
    // #582: Taste 연관 절단 — taste_id(Long) 컬럼 기준 파생 쿼리
    List<HouseTaste> findAllByTasteId(Long tasteId);
}
