package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurniture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurationRawProductFurnitureRepository
        extends JpaRepository<CurationRawProductFurniture, Long>, CurationRawProductFurnitureRepositoryCustom {

    void deleteAllByCurationRawProduct(CurationRawProduct curationRawProduct);
}
