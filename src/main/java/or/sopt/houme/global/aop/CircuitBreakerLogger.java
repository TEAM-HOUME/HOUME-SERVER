package or.sopt.houme.global.aop;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CircuitBreakerLogger {

    private final CircuitBreakerRegistry registry;

    @PostConstruct
    public void init() {
        registry.circuitBreaker("imageClient")
                .getEventPublisher()
                .onStateTransition(event ->
                        log.warn("[FAST_API_IMAGE_CLIENT] CircuitBreaker '{}' 상태가 전이되었습니다: {} -> {}",
                                event.getCircuitBreakerName(),
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState())
                );
    }
}
