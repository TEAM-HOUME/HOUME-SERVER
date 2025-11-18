package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.entity.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseRepository extends JpaRepository<House, Long>, HouseCustomRepository {
    java.util.List<House> findByUserId(Long userId);
}
