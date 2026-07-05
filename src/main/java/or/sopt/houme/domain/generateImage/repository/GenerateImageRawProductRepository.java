package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImageRawProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenerateImageRawProductRepository extends JpaRepository<GenerateImageRawProduct, Long>,
        GenerateImageRawProductRepositoryCustom {

    void deleteAllByGenerateImageIdIn(List<Long> generateImageIds);
}
