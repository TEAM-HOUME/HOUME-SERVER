package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurationRawProductColorRepository extends JpaRepository<CurationRawProductColor, Long> {
    List<CurationRawProductColor> findAllByCurationRawProductIdIn(List<Long> curationRawProductIds);

    void deleteAllByCurationRawProduct(CurationRawProduct curationRawProduct);
}
