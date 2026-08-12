package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.FurnitureRecommendBtnClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FurnitureRecommendBtnClickLogRepository extends JpaRepository<FurnitureRecommendBtnClickLog, Long> {
    void deleteByUserId(Long userId);
}
