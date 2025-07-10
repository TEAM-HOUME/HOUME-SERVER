package or.sopt.houme.domain.carousel.facade;

import jakarta.persistence.Column;
import or.sopt.houme.domain.carousel.entity.Carousel;
import or.sopt.houme.domain.carousel.repository.CarouselRepository;
import or.sopt.houme.domain.carousel.service.CarouselServiceImpl;
import or.sopt.houme.domain.preference.entity.CarouselPreference;
import or.sopt.houme.domain.preference.repository.CarouselPreferenceRepository;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CarouselException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static or.sopt.houme.global.util.constant.OptimisticLockConstant.MAX_RETRIES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CarouselOptimisticLockFacadeTest {

    @Autowired
    private CarouselOptimisticLockFacade carouselOptimisticLockFacade;

    @Autowired
    private CarouselRepository carouselRepository;

    @Autowired
    private CarouselPreferenceRepository carouselPreferenceRepository;

    @Autowired
    private UserRepository userRepository;

    @SpyBean // 이 부분 추가
    private CarouselServiceImpl carouselServiceImpl;

    private User savedUser;
    private Carousel savedCarousel;

    @BeforeEach
    @Transactional
    void setUp() {
        // 유저 저장
        savedUser = userRepository.save(
                User.builder()
                        .name("테스트유저")
                        .email("test" + UUID.randomUUID() + "@example.com")
                        .password("encoded-password")
                        .birthday(LocalDate.of(1999, 1, 1))
                        .gender(Gender.MALE)
                        .socialType(SocialType.KAKAO)
                        .status(UserStatus.ACTIVE)
                        .role(Role.ROLE_USER)
                        .hasGeneratedImage(false)
                        .build()
        );

        // 캐러셀 저장
        savedCarousel = carouselRepository.save(
                Carousel.builder()
                        .url("https://example.com/carousel.jpg")
                        .filename("carousel.jpg")
                        .originalFilename("carousel_original.jpg")
                        .fileExtension("jpg")
                        .build()
        );
    }


    @Test
    @Transactional
    @DisplayName("단일 스레드 캐러셀 좋아요 테스트")
    void testSingleThreadLikeCarousel() throws InterruptedException {
        // when
        carouselOptimisticLockFacade.likeCarousel(savedUser, savedCarousel.getId());

        // then
        List<CarouselPreference> prefs = carouselPreferenceRepository.findAll();
        assertThat(prefs).hasSize(1);
        assertThat(prefs.get(0).getPreference().isLike()).isTrue();
    }


    @Test
    @DisplayName("낙관적 락을 이용한 캐러셀 좋아요 동시성 제어 테스트")
    void testOptimisticLock_onLikeCarousel() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    carouselOptimisticLockFacade.likeCarousel(savedUser, savedCarousel.getId());
                } catch (Exception e) {
                    System.out.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        latch.await();
        // 모든 스레드가 실제로 완료될 때까지 대기
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // then: Join Fetch로 즉시 로딩
        List<CarouselPreference> prefs = carouselPreferenceRepository.findAllWithPreference();
        assertThat(prefs).hasSize(1);
        assertThat(prefs.get(0).getPreference().isLike()).isTrue();
    }


    @Test
    @DisplayName("likeCarousel()은 재시도 초과 시 CarouselException 예외를 발생시킨다")
    void testRetryExceeded_throwsCarouselException_like() throws InterruptedException {

        // given: 항상 예외 발생하도록 스텁 설정
        doThrow(new jakarta.persistence.OptimisticLockException("강제 예외"))
                .when(carouselServiceImpl)
                .likeCarousel(any(User.class), anyLong());

        // when & then
        CarouselException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                CarouselException.class,
                () -> carouselOptimisticLockFacade.likeCarousel(savedUser, savedCarousel.getId())
        );

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.CAROUSEL_RETRY_EXCEPTION);
    }


    @Test
    @DisplayName("hateCarousel()은 재시도 초과 시 CarouselException 예외를 발생시킨다")
    void testRetryExceeded_throwsCarouselException_hate() throws InterruptedException {

        // given: 항상 예외 발생하도록 스텁 설정
        doThrow(new jakarta.persistence.OptimisticLockException("강제 예외"))
                .when(carouselServiceImpl)
                .hateCarousel(any(User.class), anyLong());

        // when & then
        CarouselException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                CarouselException.class,
                () -> carouselOptimisticLockFacade.hateCarousel(savedUser, savedCarousel.getId())
        );

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.CAROUSEL_RETRY_EXCEPTION);
    }
}
