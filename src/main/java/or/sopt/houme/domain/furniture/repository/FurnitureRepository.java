package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FurnitureRepository extends JpaRepository<Furniture, Long>, FurnitureCustomRepository {

    Optional<Furniture> findByFurnitureNameKr (String furnitureName);

    // 해당 가구 타입을 가진 가구 존재 여부
    boolean existsByFurnitureType(FurnitureType type);
}