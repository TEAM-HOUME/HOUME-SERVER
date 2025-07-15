package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FurnitureTagRepository extends JpaRepository<FurnitureTag, Long> {
}
