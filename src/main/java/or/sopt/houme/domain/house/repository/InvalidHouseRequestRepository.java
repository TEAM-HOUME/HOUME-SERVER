package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.model.entity.InvalidHouseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidHouseRequestRepository extends JpaRepository<InvalidHouseRequest, Long> {
    void deleteByUserId(Long userId);
}
