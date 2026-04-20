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

    @Override
    public void run(ApplicationArguments args) {
        curationProductTokenService.initPgTrgm();
        backfill();
    }

    private void backfill() {
        long nullCount = curationProductTokenService.countNullTokenProducts();
        if (nullCount == 0) {
            log.info("search_tokens 백필 불필요: 모든 상품에 토큰이 존재합니다.");
            return;
        }

        log.info("search_tokens 백필 시작: {}건", nullCount);
        long processed = 0;

        while (true) {
            List<Long> ids = curationProductTokenService.findNullTokenProductIds(BATCH_SIZE);
            if (ids.isEmpty()) break;
            curationProductTokenService.refreshTokensForProducts(ids);
            processed += ids.size();
            log.info("search_tokens 백필 진행: {}/{}", processed, nullCount);
        }

        log.info("search_tokens 백필 완료: {}건 처리", processed);
    }
}
