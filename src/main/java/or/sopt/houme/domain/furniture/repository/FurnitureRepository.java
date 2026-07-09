package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FurnitureRepository extends JpaRepository<Furniture, Long>, FurnitureCustomRepository {

    Optional<Furniture> findByFurnitureNameKr (String furnitureName);

    List<Furniture> findAllByFurnitureTypeIdOrderByPriorityAscIdAsc(Long furnitureTypeId);

    // 해당 가구 타입을 가진 가구 존재 여부
    boolean existsByFurnitureType(FurnitureType type);
}
