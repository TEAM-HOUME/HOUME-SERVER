package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(10)
public class CurationProductTokenBackfillRunner implements ApplicationRunner {

    private static final int BATCH_SIZE = 200;

    private final CurationProductTokenService curationProductTokenService;

    private static final int MAX_ITERATIONS = 10000;

    @Override
    public void run(ApplicationArguments args) {
        curationProductTokenService.initPgTrgm();
        try {
            backfill();
        } catch (Exception e) {
            log.error("search_tokens 백필 중 오류 발생 — 서버 기동은 계속됩니다: {}", e.getMessage(), e);
        }
    }

    private void backfill() {
        long nullCount = curationProductTokenService.countNullTokenProducts();
        if (nullCount == 0) {
            log.info("search_tokens 백필 불필요: 모든 상품에 토큰이 존재합니다.");
            return;
        }

        log.info("search_tokens 백필 시작: {}건", nullCount);
        long processed = 0;
        int iterations = 0;

        while (iterations++ < MAX_ITERATIONS) {
            List<Long> ids = curationProductTokenService.findNullTokenProductIds(BATCH_SIZE);
            if (ids.isEmpty()) break;
            try {
                curationProductTokenService.refreshTokensForProducts(ids);
            } catch (Exception e) {
                log.error("search_tokens 백필 배치 오류 (ids={}): {}", ids, e.getMessage(), e);
                break;
            }
            processed += ids.size();
            log.info("search_tokens 백필 진행: {}/{}", processed, nullCount);
        }

        if (iterations >= MAX_ITERATIONS) {
            log.warn("search_tokens 백필 최대 반복 횟수 도달 — 일부 상품이 처리되지 않았을 수 있습니다.");
        } else {
            log.info("search_tokens 백필 완료: {}건 처리", processed);
        }
    }
}
