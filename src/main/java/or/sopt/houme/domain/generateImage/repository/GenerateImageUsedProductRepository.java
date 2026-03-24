package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImageUsedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenerateImageUsedProductRepository extends JpaRepository<GenerateImageUsedProduct, Long> {

    @Query("""
            select mapping
            from GenerateImageUsedProduct mapping
            join fetch mapping.curationRawProduct rawProduct
            where mapping.generateImage.id in :generateImageIds
            order by mapping.generateImage.id asc, mapping.sortOrder asc, mapping.id asc
            """)
    List<GenerateImageUsedProduct> findAllByGenerateImageIdInWithRawProduct(
            @Param("generateImageIds") List<Long> generateImageIds
    );
}
