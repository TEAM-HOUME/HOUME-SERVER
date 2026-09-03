package or.sopt.houme.compare.infra.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CurationProductSearchRepository extends JpaRepository<CurationRawProduct, Long> {

    @Query(value = """
            SELECT * FROM curation_raw_products
            WHERE image_embedding IS NOT NULL
              AND title_embedding IS NOT NULL
              AND is_exposed = true
              AND (:category IS NULL OR category = :category)
            """, nativeQuery = true)
    List<CurationRawProduct> findCandidatesByCategory(
            @Param("category") String category
    );
}
