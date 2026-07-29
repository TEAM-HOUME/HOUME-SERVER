package or.sopt.houme.global.legacy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.logging.TraceIdFilter;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LegacyApiAlertAspect {

    private final or.sopt.houme.global.legacy.LegacyApiAlertService legacyApiAlertService;

    @After("@annotation(legacyApi)")
    public void notifyLegacyApiCall(LegacyApi legacyApi) {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return;
        }

        try {
            legacyApiAlertService.notifyIfNeeded(new LegacyApiAlertCommand(
                    request.getMethod(),
                    legacyApi.documentedPath(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    legacyApi.reason(),
                    valueOrDash(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)),
                    valueOrDash(MDC.get(TraceIdFilter.USER_ID_MDC_KEY)),
                    valueOrDash(request.getHeader("User-Agent")),
                    valueOrDash(request.getHeader("Referer"))
            ));
        } catch (Exception e) {
            log.warn("레거시 API 알림 처리 실패 (무시하고 진행)", e);
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
