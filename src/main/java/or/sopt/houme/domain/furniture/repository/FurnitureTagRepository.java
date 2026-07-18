package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FurnitureTagRepository extends JpaRepository<FurnitureTag, Long>, FurnitureTagRepositoryCustom {

    Optional<FurnitureTag> findByFurnitureAndTagId(Furniture furniture, Long tagId);

    List<FurnitureTag> findByFurniture(Furniture furniture);

    List<FurnitureTag> findAllByFurnitureIdInAndTagId(List<Long> furnitureIds, Long tagId);

}
