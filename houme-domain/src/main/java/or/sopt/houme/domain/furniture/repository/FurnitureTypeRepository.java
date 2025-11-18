package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.entity.FurnitureType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FurnitureTypeRepository extends JpaRepository<FurnitureType, Long> {
    // 가구타입 한글명
    boolean existsByNameKr(String nameKr);

    // 가구타입 영어명
    boolean existsByNameEng(String nameEng);
}
