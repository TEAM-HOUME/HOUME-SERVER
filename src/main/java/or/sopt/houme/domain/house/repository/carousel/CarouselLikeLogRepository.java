package or.sopt.houme.domain.house.repository.carousel;

import or.sopt.houme.domain.house.model.carousel.entity.CarouselLikeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarouselLikeLogRepository extends JpaRepository<CarouselLikeLog, Long> {
    void deleteByUserId(Long userId);
}
