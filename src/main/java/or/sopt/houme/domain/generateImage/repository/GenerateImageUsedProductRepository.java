package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImageUsedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenerateImageUsedProductRepository extends JpaRepository<GenerateImageUsedProduct, Long>,
        GenerateImageUsedProductRepositoryCustom {
}
