package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.FurnitureRecommendBtnClickLog;
import or.sopt.houme.domain.furniture.repository.FurnitureRecommendBtnClickLogRepository;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FurnitureRecommendBtnClickLogService {
    private final FurnitureRecommendBtnClickLogRepository furnitureRecommendBtnClickLogRepository;

    public void createFurnitureRecommendBtnClickLog(UserJpaEntity user) {
        FurnitureRecommendBtnClickLog furnitureRecommendBtnClickLog = FurnitureRecommendBtnClickLog.of(user.getId());
        furnitureRecommendBtnClickLogRepository.save(furnitureRecommendBtnClickLog);
    }
}
