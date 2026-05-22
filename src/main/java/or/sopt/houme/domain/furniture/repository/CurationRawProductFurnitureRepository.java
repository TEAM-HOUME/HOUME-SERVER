package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurniture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurationRawProductFurnitureRepository extends JpaRepository<CurationRawProductFurniture, Long> {

    @Query("""
            select mapping
            from CurationRawProductFurniture mapping
            join fetch mapping.furniture furniture
            join fetch furniture.furnitureType furnitureType
            where mapping.curationRawProduct.id in :rawProductIds
            """)
    List<CurationRawProductFurniture> findAllByCurationRawProductIdInWithFurniture(
            @Param("rawProductIds") List<Long> rawProductIds
    );

    void deleteAllByCurationRawProduct(CurationRawProduct curationRawProduct);
}
