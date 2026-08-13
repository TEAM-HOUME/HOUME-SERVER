package or.sopt.houme.global.legacy;

import io.swagger.v3.oas.annotations.Operation;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.legacyapi.application.LegacyApiCallHistoryService;
import or.sopt.houme.legacyapi.domain.LegacyApiCall;
import or.sopt.houme.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("레거시 API 호출 이력 인터셉터")
class LegacyApiCallHistoryInterceptorTest {

    private final LegacyApiCallHistoryService historyService = mock(LegacyApiCallHistoryService.class);
    private final LegacyApiCallHistoryInterceptor interceptor = new LegacyApiCallHistoryInterceptor(
            historyService, Runnable::run);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    @DisplayName("레거시 API의 실제 URI와 경로 템플릿을 저장한다")
    void recordsDeprecatedCandidate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/carousels/123");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/v1/carousels/{carouselId}");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        User user = User.builder().id(42L).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null)
        );
        MDC.put("traceId", "legacy-trace-42");

        HandlerMethod handlerMethod = handlerMethod("deprecatedCandidate");
        interceptor.preHandle(request, response, handlerMethod);
        interceptor.afterCompletion(request, response, handlerMethod, null);

        ArgumentCaptor<LegacyApiCall> captor = ArgumentCaptor.forClass(LegacyApiCall.class);
        verify(historyService).record(captor.capture());
        LegacyApiCall saved = captor.getValue();
        assertThat(saved.method()).isEqualTo("GET");
        assertThat(saved.requestUri()).isEqualTo("/api/v1/carousels/123");
        assertThat(saved.apiPath()).isEqualTo("/api/v1/carousels/{carouselId}");
        assertThat(saved.userId()).isEqualTo(42L);
        assertThat(saved.traceId()).isEqualTo("legacy-trace-42");
    }

    @Test
    @DisplayName("path variable 값이 달라도 같은 레거시 API는 한 시간 동안 한 번만 저장한다")
    void coalescesSameLegacyApiPathForOneHour() throws Exception {
        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/v1/mypage/images/1");
        firstRequest.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/v1/mypage/images/{houseId}");
        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/v1/mypage/images/2");
        secondRequest.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/v1/mypage/images/{houseId}");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = handlerMethod("deprecatedCandidate");

        interceptor.preHandle(firstRequest, response, handlerMethod);
        interceptor.afterCompletion(firstRequest, response, handlerMethod, null);
        interceptor.preHandle(secondRequest, response, handlerMethod);
        interceptor.afterCompletion(secondRequest, response, handlerMethod, null);

        verify(historyService, times(1)).record(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("일반 API는 호출 이력을 저장하지 않는다")
    void skipsActiveApi() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/active");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = handlerMethod("active");

        interceptor.preHandle(request, response, handlerMethod);
        interceptor.afterCompletion(request, response, handlerMethod, null);

        verify(historyService, never()).record(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Swagger 문자열만으로는 호출 이력을 저장하지 않는다")
    void skipsSwaggerOnlyApi() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/swagger-only");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = handlerMethod("swaggerOnly");

        interceptor.preHandle(request, response, handlerMethod);
        interceptor.afterCompletion(request, response, handlerMethod, null);

        verify(historyService, never()).record(org.mockito.ArgumentMatchers.any());
    }

    private HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(new TestController(), method);
    }

    private static class TestController {

        @LegacyApi
        public void deprecatedCandidate() {
        }

        public void active() {
        }

        @Operation(summary = "[DEPRECATED_CANDIDATE] Swagger 안내만 있는 API")
        public void swaggerOnly() {
        }
    }
}
