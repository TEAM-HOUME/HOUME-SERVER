package or.sopt.houme.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("#583 traceId 추적 시스템 검증")
class TraceIdVerificationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("모든 응답에 X-Trace-Id 헤더가 포함된다")
    void everyResponseHasTraceIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/landings"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Trace-Id"));
    }

    @Test
    @DisplayName("X-Request-Id 헤더를 보내면 그대로 traceId로 수용된다")
    void acceptsClientProvidedRequestId() throws Exception {
        mockMvc.perform(get("/api/v1/landings").header("X-Request-Id", "client-trace-42"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", "client-trace-42"));
    }

    @Test
    @DisplayName("에러 응답 body에 traceId가 포함되고 헤더와 일치한다")
    void errorResponseBodyContainsTraceId() throws Exception {
        // 존재하지 않는 URL 등 에러 경로 — GlobalExceptionHandler 를 통과하는 요청
        mockMvc.perform(get("/api/v1/banners/999999/detail").header("X-Request-Id", "err-trace-77"))
                .andExpect(header().string("X-Trace-Id", "err-trace-77"))
                .andExpect(jsonPath("$.traceId").value("err-trace-77"));
    }

    @Test
    @DisplayName("성공 응답 body에는 traceId 필드가 없다 (하위호환)")
    void successResponseBodyHasNoTraceId() throws Exception {
        mockMvc.perform(get("/api/v1/landings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId").doesNotExist());
    }
}
