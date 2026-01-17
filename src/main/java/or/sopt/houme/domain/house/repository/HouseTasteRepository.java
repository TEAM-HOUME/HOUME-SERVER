package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.model.entity.mapping.HouseTaste;
import or.sopt.houme.domain.house.model.taste.entity.Taste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseTasteRepository extends JpaRepository<HouseTaste, Long> {
    void deleteByHouseId(Long houseId);
    List<HouseTaste> findAllByTaste(Taste taste);
}
