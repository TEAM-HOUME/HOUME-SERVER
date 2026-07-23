package or.sopt.houme.global.logging;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 비동기 실행 시 제출 시점의 MDC(traceId, userId)를 실행 스레드로 전파한다.
 *
 * <p>MDC 는 ThreadLocal 기반이라 {@code @Async}/{@code CompletableFuture} 로 스레드가 바뀌면
 * traceId 가 유실된다. 이 데코레이터를 executor 에 걸면 이미지 생성 같은 비동기 경로의
 * 로그도 원 요청의 traceId 로 이어진다.
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
