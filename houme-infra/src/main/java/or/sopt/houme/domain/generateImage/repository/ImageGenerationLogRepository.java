package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.model.entity.ImageGenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageGenerationLogRepository extends JpaRepository<ImageGenerationLog, Long> {
    void deleteByUserId(Long userId);
}
