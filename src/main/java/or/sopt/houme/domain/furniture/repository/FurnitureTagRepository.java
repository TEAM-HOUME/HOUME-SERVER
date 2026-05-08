package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FurnitureTagRepository extends JpaRepository<FurnitureTag, Long>, FurnitureTagRepositoryCustom {

    Optional<FurnitureTag> findByFurnitureAndTag(Furniture furniture, Tag tag);

    List<FurnitureTag> findByFurniture(Furniture furniture);

    List<FurnitureTag> findAllByFurnitureIdInAndTagId(List<Long> furnitureIds, Long tagId);

}
