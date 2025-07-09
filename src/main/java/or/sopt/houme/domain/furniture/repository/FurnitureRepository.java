package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.entity.Furniture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FurnitureRepository extends JpaRepository<Furniture, Long> {
}
