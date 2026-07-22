package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.furniture.infra.persistence.FurnitureJpaEntity;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FurnitureTagRepository extends JpaRepository<FurnitureTag, Long>, FurnitureTagRepositoryCustom {

    Optional<FurnitureTag> findByFurnitureAndTagId(FurnitureJpaEntity furniture, Long tagId);

    Optional<FurnitureTag> findByFurnitureIdAndTagId(Long furnitureId, Long tagId);

    List<FurnitureTag> findByFurniture(FurnitureJpaEntity furniture);

    List<FurnitureTag> findAllByFurnitureIdInAndTagId(List<Long> furnitureIds, Long tagId);

}
