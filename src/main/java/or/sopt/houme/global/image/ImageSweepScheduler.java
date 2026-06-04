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
 *
 * 주의: fixedDelay는 단일 JVM 안에서만 실행 겹침을 막으므로, 서버를 여러 대로 운영하면
 * 각 인스턴스가 같은 prefix를 중복 sweep합니다(멱등 key라 데이터는 안전하나 자원 낭비).
 * 다중 인스턴스 시 image.sweep.enabled로 한 인스턴스에서만 켜거나 분산 락을 두세요.
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
