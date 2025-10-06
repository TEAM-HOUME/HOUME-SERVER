package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.entity.ImageGenerationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageGenerationDetailRepository extends JpaRepository<ImageGenerationDetail, Long> {
}
