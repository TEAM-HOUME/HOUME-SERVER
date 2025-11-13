package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.entity.mapping.HouseTaste;
import or.sopt.houme.domain.taste.entity.Taste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseTasteRepository extends JpaRepository<HouseTaste, Long> {
    void deleteByHouseId(Long houseId);
    Optional<HouseTaste> findByTaste(Taste taste);
}
