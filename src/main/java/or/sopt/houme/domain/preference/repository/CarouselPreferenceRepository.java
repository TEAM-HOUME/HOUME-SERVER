package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.CarouselPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarouselPreferenceRepository extends JpaRepository<CarouselPreference, Long> {

    boolean existsByUserIdAndCarouselId(Long userId, Long carouselId);
    Optional<CarouselPreference> findByUserIdAndCarouselId(Long userId, Long carouselId);
}
