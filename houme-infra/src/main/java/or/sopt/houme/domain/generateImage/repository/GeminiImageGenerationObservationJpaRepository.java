package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.model.entity.GeminiImageGenerationObservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeminiImageGenerationObservationJpaRepository
        extends JpaRepository<GeminiImageGenerationObservationJpaEntity, Long> {
}
