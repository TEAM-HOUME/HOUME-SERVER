package or.sopt.houme.domain.carousel.facade;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.carousel.service.CarouselServiceImpl;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CarouselException;
import org.springframework.stereotype.Component;

import org.springframework.dao.DataIntegrityViolationException;

import static or.sopt.houme.global.util.constant.OptimisticLockConstant.MAX_RETRIES;
import static or.sopt.houme.global.util.constant.OptimisticLockConstant.RETRY_DELAY_MS;

@Component
@RequiredArgsConstructor
public class CarouselOptimisticLockFacade {

    private final CarouselServiceImpl preferenceService;

    public void likeCarousel(User user, Long carouselId) throws InterruptedException {
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                preferenceService.likeCarousel(user, carouselId);
                return;
            } catch (OptimisticLockException | DataIntegrityViolationException e) {
                retryCount++;
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        throw new CarouselException(ErrorCode.CAROUSEL_RETRY_EXCEPTION);
    }

    public void hateCarousel(User user, Long carouselId) throws InterruptedException {
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                preferenceService.hateCarousel(user, carouselId);
                return;
            } catch (OptimisticLockException | DataIntegrityViolationException e) {
                retryCount++;
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        throw new CarouselException(ErrorCode.CAROUSEL_RETRY_EXCEPTION);
    }
}
