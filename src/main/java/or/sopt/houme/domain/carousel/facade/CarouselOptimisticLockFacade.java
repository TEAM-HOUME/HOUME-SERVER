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

    /**
     * likeCarousel() 메서드를 실행합니다
     *
     * 실행 중 @Version 이 일치하지 않으면 끝까지 실행된 트랜잭션을 이전으로 롤백시키는 방식으로 동시성을 제어합니다
     * 동시성에 걸리면 스레드를 일정시간 sleep 시켜놓습니다
     * sleep 되는 시간은 지수적으로 증가하여 DB에 불필요한 접근을 방지합니다
     * 스레드가 sleep 된 횟수가 일정횟수에 도달하면 예외를 발생시킵니다
     *
     * 이는 hateCarousel() 도 동일합니다
     * */
    public void likeCarousel(User user, Long carouselId) throws InterruptedException {
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                preferenceService.likeCarousel(user, carouselId);
                return;
            } catch (OptimisticLockException | DataIntegrityViolationException e) {
                long backoffTime = (long) Math.pow(2, retryCount) * RETRY_DELAY_MS;
                Thread.sleep(backoffTime);
                retryCount++;
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
                long backoffTime = (long) Math.pow(2, retryCount) * RETRY_DELAY_MS;
                Thread.sleep(backoffTime);
                retryCount++;
            }
        }

        throw new CarouselException(ErrorCode.CAROUSEL_RETRY_EXCEPTION);
    }
}
