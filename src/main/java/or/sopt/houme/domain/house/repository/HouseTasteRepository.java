package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.entity.mapping.HouseTaste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseTasteRepository extends JpaRepository<HouseTaste, Long> {
}
