package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationProductSearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurationProductSearchKeywordRepository extends JpaRepository<CurationProductSearchKeyword, Long> {
    List<CurationProductSearchKeyword> findAllByCurationRawProductId(Long productId);
    List<CurationProductSearchKeyword> findAllByCurationRawProductIdIn(List<Long> productIds);
}
