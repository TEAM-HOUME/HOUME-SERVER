package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CurationRawProductRepositoryCustom {

    Page<CurationRawProduct> searchByKeyword(String keyword, Pageable pageable);
}
