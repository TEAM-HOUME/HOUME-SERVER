package or.sopt.houme.domain.furniture.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.service.CurationProductTokenService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurationProductTokenEventListener {

    private final CurationProductTokenService curationProductTokenService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("tokenRefreshExecutor")
    public void handleTokenRefresh(CurationRawProductTokenRefreshEvent event) {
        try {
            curationProductTokenService.refreshTokensForProducts(event.productIds());
        } catch (Exception e) {
            log.error("search_tokens 갱신 실패 (productIds={}): {}", event.productIds(), e.getMessage(), e);
        }
    }
}
