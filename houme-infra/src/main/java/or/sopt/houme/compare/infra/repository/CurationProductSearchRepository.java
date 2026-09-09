package or.sopt.houme.compare.infra.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurationProductSearchRepository extends JpaRepository<CurationRawProduct, Long> {
}
