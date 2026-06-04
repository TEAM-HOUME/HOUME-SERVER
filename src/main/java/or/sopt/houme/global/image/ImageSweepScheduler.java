package or.sopt.houme.global.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 어드민 이미지 최적화 sweep을 주기적으로 실행하는 스케줄러입니다.
 *
 * fixedDelay(기본 12시간) 간격으로 sweep을 실행합니다.
 * image.sweep.enabled=false 로 비활성화할 수 있습니다(테스트/로컬 환경).
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "image.sweep", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ImageSweepScheduler {

    private final ImageSweepService imageSweepService;

    @Scheduled(
            // 서버 시작 60초 후 첫 sweep 돌리기
            initialDelayString = "${image.sweep.initial-delay-ms:60000}",
            fixedDelayString = "${image.sweep.fixed-delay-ms:43200000}"
    )
    public void runSweep() {
        imageSweepService.sweep();
    }
}
