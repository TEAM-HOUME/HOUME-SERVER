package or.sopt.houme.domain.carousel.facade;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.carousel.service.CarouselServiceImpl;
import or.sopt.houme.domain.user.entity.User;
import org.springframework.stereotype.Component;

import org.springframework.dao.DataIntegrityViolationException;

@Component
@RequiredArgsConstructor
public class CarouselOptimisticLockFacade {

    private final CarouselServiceImpl preferenceService;

    public void likeCarousel(User user, Long carouselId) throws InterruptedException {
        while (true) {
            try {
                preferenceService.likeCarousel(user, carouselId);
                break;
            } catch (OptimisticLockException | DataIntegrityViolationException e) {
                Thread.sleep(50);
            }
        }
    }

    public void hateCarousel(User user, Long carouselId) throws InterruptedException {
        while (true) {
            try {
                preferenceService.hateCarousel(user, carouselId);
                break;
            } catch (OptimisticLockException | DataIntegrityViolationException e) {
                Thread.sleep(50);
            }
        }
    }
}
