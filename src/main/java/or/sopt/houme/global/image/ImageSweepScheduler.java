package or.sopt.houme.global.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 어드민 이미지 최적화 sweep을 주기적으로 실행하는 스케줄러입니다.
 *
 * 매일 새벽 4시(KST)에 sweep을 실행합니다. (트래픽이 적은 시간대 + 24시간 주기)
 * image.sweep.enabled=false 로 비활성화할 수 있습니다(테스트/로컬 환경).
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "image.sweep", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ImageSweepScheduler {

    private final ImageSweepService imageSweepService;

    // 매일 새벽 4시(KST) 실행. cron 형식: 초 분 시 일 월 요일
    @Scheduled(cron = "${image.sweep.cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void runSweep() {
        // 예기치 못한 Throwable(Error 포함)이 스케줄러를 조용히 멈추지 않도록 최상위 가드
        try {
            imageSweepService.sweep();
        } catch (Throwable t) {
            log.error("이미지 최적화 sweep 실행 중 예외 발생. 다음 주기에 재시도합니다.", t);
        }
    }
}
